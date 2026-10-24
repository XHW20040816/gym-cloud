package com.gym.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gym.api.dto.SeckillOrderDTO;
import com.gym.common.constant.MQConstants;
import com.gym.common.constant.RedisConstants;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.entity.seckill.SeckillOrder;
import com.gym.seckill.mapper.SeckillGoodsMapper;
import com.gym.seckill.mapper.SeckillOrderMapper;
import com.gym.seckill.service.SeckillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 秒杀异步建单消费者
 * 接收 doSeckill 发送到 SECKILL_ORDER_QUEUE 的 JSON 消息：
 *   → 先投递 TTL 死信取消消息（ORDER_TTL_EXCHANGE → 过期后进 SECKILL_DLX_QUEUE）
 *   → 再在一个本地事务里写 t_seckill_order + t_seckill_goods sold/stock
 *
 * <p>一致性保证：
 * <ol>
 *   <li>幂等：同 orderNo 已入库说明重复投递，直接 ACK，不重复扣 DB 库存；</li>
 *   <li>TTL 消息先发后落库：发送失败时 DB 尚未写入，回滚 Redis 后重试即可；落库失败时
 *       TTL 消息对应的订单不存在，超时消费者查不到订单会自动忽略，不会出现「订单已入库但无超时兜底」；</li>
 *   <li>唯一键兜底：uk_order_no 冲突（重复投递）不回滚 Redis；uk_user_goods 冲突（Redis 与 DB
 *       口径不一致）必须回滚本次 Redis 扣减；</li>
 *   <li>消费失败首次重回队列重试一次，重试仍失败才回滚 Redis 并放弃，避免无限重投。</li>
 * </ol>
 */
@Slf4j
@Component
@RabbitListener(queues = MQConstants.SECKILL_ORDER_QUEUE)
public class SeckillOrderCreateConsumer {

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    @Qualifier("seckillRollbackScript")
    private DefaultRedisScript<Long> seckillRollbackScript;

    @RabbitHandler
    public void onMessage(String json, Channel channel, Message message) {
        long tag = message.getMessageProperties().getDeliveryTag();

        SeckillOrderDTO dto;
        try {
            dto = JSON.parseObject(json, SeckillOrderDTO.class);
        } catch (Exception e) {
            log.error("秒杀建单消息JSON解析失败，无法处理，直接ACK: {}", json, e);
            ack(channel, tag);
            return;
        }

        try {
            log.info("收到秒杀异步建单消息: {}", JSONObject.toJSONString(dto));

            // ① 幂等：同 orderNo 已入库 → 重复投递，ACK 且不再扣库存
            Long exists = seckillOrderMapper.selectCount(
                    Wrappers.<SeckillOrder>lambdaQuery().eq(SeckillOrder::getOrderNo, dto.getOrderNo()));
            if (exists != null && exists > 0) {
                log.info("秒杀建单消息重复投递，订单已存在，直接ACK orderNo={}", dto.getOrderNo());
                ack(channel, tag);
                return;
            }

            // ② 秒杀商品不存在：Redis 已扣，必须回滚后 ACK
            SeckillGoods goods = seckillGoodsMapper.selectById(dto.getGoodsId());
            if (goods == null) {
                log.error("秒杀商品不存在，回滚Redis后跳过建单: goodsId={}", dto.getGoodsId());
                rollbackRedis(dto.getGoodsId(), dto.getUserId());
                ack(channel, tag);
                return;
            }

            // ③ 先投递 TTL 超时取消消息，再落库（顺序见类注释）
            sendTtlMessage(dto.getOrderNo());

            // ④ 建单 + DB 扣库存（同一本地事务）
            seckillOrderService.createSeckillOrder(dto, goods.getSeckillPrice());
            log.info("秒杀订单创建成功 orderNo={}, TTL={}ms（由 seckill.ttl.queue 队列级 TTL 决定）",
                    dto.getOrderNo(), MQConstants.ORDER_TTL);

            ack(channel, tag);
        } catch (DuplicateKeyException e) {
            // uk_order_no 冲突 → 重复投递，不回滚；uk_user_goods 冲突 → 本次 Redis 扣减作废，必须回滚
            if (orderExists(dto.getOrderNo())) {
                log.warn("秒杀订单重复投递（uk_order_no冲突），不回滚Redis: orderNo={}", dto.getOrderNo());
            } else {
                log.warn("同一用户同一商品已有订单（uk_user_goods冲突），回滚本次Redis扣减: dto={}",
                        JSONObject.toJSONString(dto));
                rollbackRedis(dto.getGoodsId(), dto.getUserId());
            }
            ack(channel, tag);
        } catch (Exception e) {
            log.error("秒杀订单创建失败: dto={}", JSONObject.toJSONString(dto), e);
            boolean redelivered = Boolean.TRUE.equals(message.getMessageProperties().getRedelivered());
            if (!redelivered) {
                // 首次失败：保留 Redis 扣减，消息重回队列重试一次
                nack(channel, tag, true, dto);
            } else {
                // 重试仍失败：回滚 Redis 扣减后放弃，ERROR 日志保留消息体供人工补偿
                rollbackRedis(dto.getGoodsId(), dto.getUserId());
                nack(channel, tag, false, dto);
            }
        }
    }

    /**
     * 投递 TTL 死信取消消息：走独立的 SECKILL_TTL_EXCHANGE → 过期后进 SECKILL_DLX_QUEUE。
     * TTL 只由队列级 x-message-ttl 决定（单一来源），消息上不再单独设置过期时间，
     * 避免「队列级 + 消息级」两个值取最小值导致行为不确定。
     */
    private void sendTtlMessage(String orderNo) {
        rabbitTemplate.convertAndSend(
                MQConstants.SECKILL_TTL_EXCHANGE,
                MQConstants.SECKILL_TTL_QUEUE,
                orderNo
        );
    }

    private boolean orderExists(String orderNo) {
        Long count = seckillOrderMapper.selectCount(
                Wrappers.<SeckillOrder>lambdaQuery().eq(SeckillOrder::getOrderNo, orderNo));
        return count != null && count > 0;
    }

    private void ack(Channel channel, long tag) {
        try {
            channel.basicAck(tag, false);
        } catch (IOException e) {
            log.error("秒杀建单消息ACK失败 tag={}", tag, e);
        }
    }

    private void nack(Channel channel, long tag, boolean requeue, SeckillOrderDTO dto) {
        try {
            channel.basicNack(tag, false, requeue);
            if (!requeue) {
                log.error("秒杀建单消息重试后仍失败，已丢弃（Redis已回滚），需人工补偿: orderNo={}, userId={}, goodsId={}",
                        dto.getOrderNo(), dto.getUserId(), dto.getGoodsId());
            }
        } catch (IOException e) {
            log.error("秒杀建单消息NACK失败 tag={}", tag, e);
        }
    }

    /** 回滚 Redis：库存+1 / 移除售罄标记 / 用户购买次数-1 */
    private void rollbackRedis(Long seckillGoodsId, Long userId) {
        try {
            List<String> keys = Arrays.asList(
                    RedisConstants.SECKILL_STOCK_KEY + seckillGoodsId,
                    RedisConstants.SECKILL_USER_KEY + seckillGoodsId,
                    RedisConstants.SECKILL_SOLDOUT_KEY + seckillGoodsId
            );
            stringRedisTemplate.execute(seckillRollbackScript, keys, String.valueOf(userId));
            log.info("秒杀建单失败，Redis回滚成功 goodsId={}, userId={}", seckillGoodsId, userId);
        } catch (Exception e) {
            log.error("秒杀建单失败，Redis回滚异常 goodsId={}, userId={}", seckillGoodsId, userId, e);
        }
    }
}
