package com.gym.seckill.runner;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gym.common.constant.RedisConstants;
import com.gym.entity.seckill.SeckillActivity;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.seckill.mapper.SeckillActivityMapper;
import com.gym.seckill.mapper.SeckillGoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 秒杀库存预热 Runner
 * 服务启动时将未开始/进行中的秒杀活动及其商品库存加载到 Redis，
 * 避免秒杀请求瞬间全部打入数据库。
 */
@Slf4j
@Component
@Order(1)
public class SeckillStockWarmRunner implements CommandLineRunner {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public void run(String... args) {
        // 未开始(0) + 进行中(1) 的活动需要预热
        List<SeckillActivity> activities = seckillActivityMapper.selectList(
                Wrappers.<SeckillActivity>lambdaQuery()
                        .in(SeckillActivity::getStatus,
                                SeckillActivity.STATUS_NOT_START,
                                SeckillActivity.STATUS_ONGOING)
        );
        if (activities == null || activities.isEmpty()) {
            log.info("秒杀库存预热：暂无进行中/未开始的秒杀活动");
            return;
        }

        for (SeckillActivity activity : activities) {
            List<SeckillGoods> goodsList = seckillGoodsMapper.selectList(
                    Wrappers.<SeckillGoods>lambdaQuery()
                            .eq(SeckillGoods::getActivityId, activity.getId())
            );
            if (goodsList == null || goodsList.isEmpty()) {
                continue;
            }
            for (SeckillGoods goods : goodsList) {
                // 使用秒杀商品主键 ID 维度做库存 Key，便于 AC-5 多级防护
                String stockKey = RedisConstants.SECKILL_STOCK_KEY + goods.getId();
                String userHashKey = RedisConstants.SECKILL_USER_KEY + goods.getId();
                String soldoutKey = RedisConstants.SECKILL_SOLDOUT_KEY + goods.getId();

                // 如果 Redis 已有库存键，则不覆盖（防止重启清零已售部分）
                Boolean exists = stringRedisTemplate.hasKey(stockKey);
                if (Boolean.FALSE.equals(exists)) {
                    stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
                    // 重建用户购买记录hash和售罄标记（和库存保持一致）
                    stringRedisTemplate.delete(userHashKey);
                    stringRedisTemplate.delete(soldoutKey);
                }
            }
            log.info("秒杀活动{}({}) 库存预热完成，共{}个商品",
                    activity.getName(), activity.getId(), goodsList.size());
        }
        log.info("秒杀库存预热全部完成，活动数={}", activities.size());
    }
}
