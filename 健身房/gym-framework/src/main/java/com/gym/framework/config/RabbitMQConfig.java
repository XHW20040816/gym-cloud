package com.gym.framework.config;

import com.gym.common.constant.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // ============================ 秒杀订单 ============================

    @Bean("seckillOrderExchange")
    public DirectExchange seckillOrderExchange() {
        return ExchangeBuilder.directExchange(MQConstants.SECKILL_ORDER_EXCHANGE).durable(true).build();
    }

    @Bean("seckillOrderQueue")
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(MQConstants.SECKILL_ORDER_QUEUE).build();
    }

    @Bean
    public Binding seckillOrderBinding(@Qualifier("seckillOrderQueue") Queue queue,
                                       @Qualifier("seckillOrderExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.SECKILL_ORDER_QUEUE);
    }

    // ============================ 订单TTL+死信 ============================

    @Bean("orderTtlExchange")
    public DirectExchange orderTtlExchange() {
        return ExchangeBuilder.directExchange(MQConstants.ORDER_TTL_EXCHANGE).durable(true).build();
    }

    @Bean("orderTtlQueue")
    public Queue orderTtlQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-message-ttl", MQConstants.ORDER_TTL);
        args.put("x-dead-letter-exchange", MQConstants.ORDER_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQConstants.ORDER_DLX_QUEUE);
        return QueueBuilder.durable(MQConstants.ORDER_TTL_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding orderTtlBinding(@Qualifier("orderTtlQueue") Queue queue,
                                   @Qualifier("orderTtlExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.ORDER_TTL_QUEUE);
    }

    @Bean("orderDlxExchange")
    public DirectExchange orderDlxExchange() {
        return ExchangeBuilder.directExchange(MQConstants.ORDER_DLX_EXCHANGE).durable(true).build();
    }

    @Bean("orderDlxQueue")
    public Queue orderDlxQueue() {
        return QueueBuilder.durable(MQConstants.ORDER_DLX_QUEUE).build();
    }

    @Bean
    public Binding orderDlxBinding(@Qualifier("orderDlxQueue") Queue queue,
                                   @Qualifier("orderDlxExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.ORDER_DLX_QUEUE);
    }

    // ============================ 商品同步ES（Fanout） ============================

    @Bean("goodsExchange")
    public FanoutExchange goodsExchange() {
        return ExchangeBuilder.fanoutExchange(MQConstants.GOODS_EXCHANGE).durable(true).build();
    }

    @Bean("goodsEsQueue")
    public Queue goodsEsQueue() {
        return QueueBuilder.durable(MQConstants.GOODS_QUEUE).build();
    }

    @Bean
    public Binding goodsEsBinding(@Qualifier("goodsEsQueue") Queue queue,
                                  @Qualifier("goodsExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // ============================ 秒杀订单独立TTL+死信 ============================
    // 与普通订单的死信队列隔离，避免 RabbitMQ 轮询投递导致秒杀超时消息被普通订单消费者误ACK

    @Bean("seckillTtlExchange")
    public DirectExchange seckillTtlExchange() {
        return ExchangeBuilder.directExchange(MQConstants.SECKILL_TTL_EXCHANGE).durable(true).build();
    }

    @Bean("seckillTtlQueue")
    public Queue seckillTtlQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-message-ttl", MQConstants.ORDER_TTL);
        args.put("x-dead-letter-exchange", MQConstants.SECKILL_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQConstants.SECKILL_DLX_QUEUE);
        return QueueBuilder.durable(MQConstants.SECKILL_TTL_QUEUE).withArguments(args).build();
    }

    @Bean
    public Binding seckillTtlBinding(@Qualifier("seckillTtlQueue") Queue queue,
                                     @Qualifier("seckillTtlExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.SECKILL_TTL_QUEUE);
    }

    @Bean("seckillDlxExchange")
    public DirectExchange seckillDlxExchange() {
        return ExchangeBuilder.directExchange(MQConstants.SECKILL_DLX_EXCHANGE).durable(true).build();
    }

    @Bean("seckillDlxQueue")
    public Queue seckillDlxQueue() {
        return QueueBuilder.durable(MQConstants.SECKILL_DLX_QUEUE).build();
    }

    @Bean
    public Binding seckillDlxBinding(@Qualifier("seckillDlxQueue") Queue queue,
                                     @Qualifier("seckillDlxExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.SECKILL_DLX_QUEUE);
    }
}
