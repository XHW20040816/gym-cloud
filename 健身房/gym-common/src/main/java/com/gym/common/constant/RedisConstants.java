package com.gym.common.constant;

public interface RedisConstants {

    String SECKILL_STOCK_KEY = "seckill:stock:";
    String SECKILL_USER_KEY = "seckill:user:";
    String SECKILL_SOLDOUT_KEY = "seckill:soldout:";
    String SECKILL_LOCK_KEY = "seckill:lock:";

    String TOKEN_KEY = "auth:token:";
    String PAY_LOCK_KEY = "pay:lock:";

    /** 空值缓存TTL（分钟） */
    Long CACHE_NULL_TTL = 2L;
    /** 普通缓存TTL（分钟） */
    Long CACHE_TTL = 30L;
}
