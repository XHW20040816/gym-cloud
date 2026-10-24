package com.gym.seckill.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gym.common.constant.MQConstants;
import com.gym.common.constant.RedisConstants;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.seckill.SeckillOrder;
import com.gym.seckill.mapper.SeckillOrderMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 秒杀订单死信超时取消消费者
 * 监听独立的 SECKILL_DLX_QUEUE（与普通订单死信队列隔离）：
 *   收到 orderNo → 查 t_seckill_order → UNPAID → CANCELLED → Lua 回滚 Redis
 */
@Slf4j
@Component
@RabbitListener(queues = MQConstants.SECKILL_DLX_QUEUE)
public class SeckillTimeoutConsumer {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    @Qualifier("seckillRollbackScript")
    private DefaultRedisScript<Long> seckillRollbackScript;

    @RabbitHandler
    public void handleTimeout(String orderNo, Channel channel, Message message) {
        try {
            SeckillOrder order = seckillOrderMapper.selectOne(
                    Wrappers.<SeckillOrder>lambdaQuery()
                            .eq(SeckillOrder::getOrderNo, orderNo)
            );
            if (order == null) {
                log.warn("秒杀超时取消：订单不存在 orderNo={}", orderNo);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            if (order.getStatus() == OrderStatus.UNPAID) {
                order.setStatus(OrderStatus.CANCELLED);
                seckillOrderMapper.updateById(order);

                // 回滚 Redis 库存
                List<String> keys = Arrays.asList(
                        RedisConstants.SECKILL_STOCK_KEY + order.getGoodsId(),
                        RedisConstants.SECKILL_USER_KEY + order.getGoodsId(),
                        RedisConstants.SECKILL_SOLDOUT_KEY + order.getGoodsId()
                );
                try {
                    stringRedisTemplate.execute(seckillRollbackScript, keys, String.valueOf(order.getUserId()));
                } catch (Exception e) {
                    log.error("秒杀超时取消，Redis回滚异常 orderNo={}", orderNo, e);
                }
                log.info("秒杀订单超时取消完成 orderNo={}, userId={}, goodsId={}",
                        orderNo, order.getUserId(), order.getGoodsId());
            } else {
                log.info("秒杀订单非未支付状态，跳过超时取消 orderNo={}, status={}", orderNo, order.getStatus());
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("秒杀订单超时取消异常 orderNo={}", orderNo, e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("秒杀超时取消消息NACK失败 orderNo={}", orderNo, ex);
            }
        }
    }
}
