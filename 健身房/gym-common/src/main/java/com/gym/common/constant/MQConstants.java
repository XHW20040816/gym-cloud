package com.gym.common.constant;

public interface MQConstants {

    /** 秒杀订单交换机 */
    String SECKILL_ORDER_EXCHANGE = "seckill.order.exchange";
    /** 秒杀订单队列 */
    String SECKILL_ORDER_QUEUE = "seckill.order.queue";

    /** 订单TTL交换机 */
    String ORDER_TTL_EXCHANGE = "order.ttl.exchange";
    /** 订单TTL队列 */
    String ORDER_TTL_QUEUE = "order.ttl.queue";
    /** 订单死信交换机 */
    String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    /** 订单死信队列 */
    String ORDER_DLX_QUEUE = "order.dlx.queue";
    /** 订单TTL（30分钟，毫秒） */
    Integer ORDER_TTL = 1800000;

    /** 秒杀订单TTL交换机（独立于普通订单，避免死信队列轮询错误消费） */
    String SECKILL_TTL_EXCHANGE = "seckill.ttl.exchange";
    /** 秒杀订单TTL队列 */
    String SECKILL_TTL_QUEUE = "seckill.ttl.queue";
    /** 秒杀订单死信交换机 */
    String SECKILL_DLX_EXCHANGE = "seckill.dlx.exchange";
    /** 秒杀订单死信队列 */
    String SECKILL_DLX_QUEUE = "seckill.dlx.queue";

    /** 商品同步ES扇形交换机 */
    String GOODS_EXCHANGE = "goods.exchange.fanout";
    /** 商品ES同步队列 */
    String GOODS_QUEUE = "goods.es.sync.queue";
}
