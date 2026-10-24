package com.gym.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.api.dto.SeckillOrderDTO;
import com.gym.api.goods.GoodsFeignClient;
import com.gym.common.constant.MQConstants;
import com.gym.common.constant.RedisConstants;
import com.gym.common.exception.BizException;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.common.util.SnowflakeIdUtil;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.seckill.SeckillActivity;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.entity.seckill.SeckillOrder;
import com.gym.seckill.mapper.SeckillActivityMapper;
import com.gym.seckill.mapper.SeckillGoodsMapper;
import com.gym.seckill.mapper.SeckillOrderMapper;
import com.gym.seckill.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillOrderServiceImpl
        extends ServiceImpl<SeckillOrderMapper, SeckillOrder>
        implements SeckillOrderService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    @Qualifier("seckillStockScript")
    private DefaultRedisScript<Long> seckillStockScript;

    @Resource
    @Qualifier("seckillRollbackScript")
    private DefaultRedisScript<Long> seckillRollbackScript;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Resource
    private GoodsFeignClient goodsFeignClient;

    @Resource
    private SnowflakeIdUtil snowflakeIdUtil;

    @Override
    public R<Long> doSeckill(Long userId, Long seckillGoodsId) {
        // ① 查秒杀商品，非空校验 → 取 activityId
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(seckillGoodsId);
        if (seckillGoods == null) {
            return R.fail(ResultCode.PARAM_ERROR, "秒杀商品不存在");
        }
        Long activityId = seckillGoods.getActivityId();

        // ② 查活动：当前时间窗口校验
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            return R.fail(ResultCode.PARAM_ERROR, "秒杀活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            return R.fail(ResultCode.FAIL, "活动不在时间窗口");
        }
        if (Objects.equals(activity.getStatus(), SeckillActivity.STATUS_ENDED)) {
            return R.fail(ResultCode.SECKILL_END);
        }

        // ③ 快速失败：售罄标记检查（Redis存在就直接返回，防穿透）
        Boolean sold = stringRedisTemplate.hasKey(RedisConstants.SECKILL_SOLDOUT_KEY + seckillGoodsId);
        if (Boolean.TRUE.equals(sold)) {
            return R.fail(ResultCode.SECKILL_END);
        }

        // ④ Lua 原子执行：售罄判断 → 用户限购判断 → 库存扣减 + 用户登记 + 售罄写标记
        List<String> keys = Arrays.asList(
                RedisConstants.SECKILL_STOCK_KEY + seckillGoodsId,
                RedisConstants.SECKILL_USER_KEY + seckillGoodsId,
                RedisConstants.SECKILL_SOLDOUT_KEY + seckillGoodsId
        );
        Long result;
        try {
            result = stringRedisTemplate.execute(seckillStockScript, keys,
                    String.valueOf(userId), String.valueOf(activity.getLimitCount()));
        } catch (Exception e) {
            log.error("Lua秒杀脚本执行异常 userId={}, goodsId={}", userId, seckillGoodsId, e);
            return R.fail(ResultCode.FAIL, "秒杀异常，请稍后重试");
        }
        if (result == null) {
            return R.fail(ResultCode.FAIL, "秒杀异常，库存脚本无返回");
        }
        if (result == -1L) {
            return R.fail(ResultCode.SECKILL_END);
        }
        if (result == -2L) {
            return R.fail(ResultCode.REPEAT_SECKILL);
        }

        // ⑤ Redisson 分布式锁兜底（Lua已保证，再防极端DB唯一索引冲突前被别的路径插入）
        String lockKey = RedisConstants.SECKILL_LOCK_KEY + userId + ":" + seckillGoodsId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(0, 10, TimeUnit.SECONDS)) {
                // 没拿到锁 → 认为重复秒杀
                rollbackRedis(seckillGoodsId, userId);
                return R.fail(ResultCode.REPEAT_SECKILL);
            }

            // 异步创建订单：发送 RabbitMQ 消息到 SECKILL_ORDER_QUEUE（不是TTL队列）
            String orderNo = snowflakeIdUtil.nextIdStr();
            SeckillOrderDTO dto = SeckillOrderDTO.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .goodsId(seckillGoodsId)
                    .orderNo(orderNo)
                    .build();
            // routing key 与 framework 的 binding 一致
            rabbitTemplate.convertAndSend(
                    MQConstants.SECKILL_ORDER_EXCHANGE,
                    MQConstants.SECKILL_ORDER_QUEUE,
                    JSON.toJSONString(dto)
            );
            log.info("秒杀排队成功，已发送异步建单消息 userId={}, seckillGoodsId={}, orderNo={}",
                    userId, seckillGoodsId, orderNo);

            return R.ok(seckillGoodsId); // 返回排队中的商品ID
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            rollbackRedis(seckillGoodsId, userId);
            return R.fail(ResultCode.FAIL, "秒杀被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public IPage<SeckillOrder> pageMy(Long userId, Page<SeckillOrder> page) {
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getUserId, userId);
        wrapper.orderByDesc(SeckillOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public SeckillOrder getByUserAndGoods(Long userId, Long seckillGoodsId) {
        // 先查 seckillGoods → activityId，再用 countByActivityAndUser 也是一种方法，
        // 这里直接按 userId + goodsId 查询更直接（一个用户同一 seckillGoods 只能有一条 UNPAID/PAID 记录）
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getUserId, userId);
        wrapper.eq(SeckillOrder::getGoodsId, seckillGoodsId);
        // 不包括已取消/已退款
        wrapper.notIn(SeckillOrder::getStatus, OrderStatus.CANCELLED, OrderStatus.REFUNDED);
        wrapper.orderByDesc(SeckillOrder::getCreateTime);
        wrapper.last("LIMIT 1");
        return getOne(wrapper, false);
    }

    @Override
    public void seckillTimeoutCancel(String orderNo) {
        SeckillOrder order = seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>().eq(SeckillOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            log.warn("秒杀超时取消：订单不存在 orderNo={}", orderNo);
            return;
        }
        if (order.getStatus() != OrderStatus.UNPAID) {
            log.info("秒杀超时取消：订单状态非未支付 orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }
        // 置已取消
        order.setStatus(OrderStatus.CANCELLED);
        seckillOrderMapper.updateById(order);

        // 回滚 Redis 库存
        rollbackRedis(order.getGoodsId(), order.getUserId());
        log.info("秒杀订单超时取消完成 orderNo={}, userId={}, goodsId={}",
                orderNo, order.getUserId(), order.getGoodsId());
    }

    // ================== 内部工具方法 ==================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(SeckillOrderDTO dto, BigDecimal amount) {
        SeckillOrder order = new SeckillOrder();
        order.setOrderNo(dto.getOrderNo());
        order.setActivityId(dto.getActivityId());
        order.setGoodsId(dto.getGoodsId());
        order.setUserId(dto.getUserId());
        order.setAmount(amount);
        order.setStatus(OrderStatus.UNPAID);
        seckillOrderMapper.insert(order);

        int rows = seckillGoodsMapper.addSold(dto.getGoodsId(), 1);
        if (rows == 0) {
            // 订单插入与库存扣减必须同生共死，库存扣不动就让整个本地事务回滚
            throw new BizException(ResultCode.SECKILL_END, "秒杀商品DB库存不足，建单回滚");
        }
        log.info("秒杀订单落库成功 orderNo={}, userId={}, goodsId={}", dto.getOrderNo(), dto.getUserId(), dto.getGoodsId());
    }

    /** Lua 回滚：库存+1、移除售罄标记、用户购买次数-1 */
    private void rollbackRedis(Long seckillGoodsId, Long userId) {
        try {
            List<String> keys = Arrays.asList(
                    RedisConstants.SECKILL_STOCK_KEY + seckillGoodsId,
                    RedisConstants.SECKILL_USER_KEY + seckillGoodsId,
                    RedisConstants.SECKILL_SOLDOUT_KEY + seckillGoodsId
            );
            stringRedisTemplate.execute(seckillRollbackScript, keys, String.valueOf(userId));
            log.info("秒杀Redis库存回滚成功 seckillGoodsId={}, userId={}", seckillGoodsId, userId);
        } catch (Exception e) {
            log.error("秒杀Redis库存回滚异常 seckillGoodsId={}, userId={}", seckillGoodsId, userId, e);
        }
    }
}
