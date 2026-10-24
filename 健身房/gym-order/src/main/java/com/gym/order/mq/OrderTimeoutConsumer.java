package com.gym.order.mq;

import com.gym.common.constant.MQConstants;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.order.Orders;
import com.gym.order.service.OrdersService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 订单超时取消死信消费者
 * 订单创建时发送 TTL 消息到 ORDER_TTL_QUEUE（默认30分钟过期），
 * 过期后消息死信转发到 ORDER_DLX_QUEUE，本监听器消费后自动取消未支付订单。
 */
@Slf4j
@Component
@RabbitListener(queues = MQConstants.ORDER_DLX_QUEUE)
public class OrderTimeoutConsumer {

    @Resource
    private OrdersService ordersService;

    @RabbitHandler
    public void handleOrderTimeout(String orderNo, Channel channel, Message message) {
        log.info("收到订单超时取消死信消息: {}", orderNo);
        try {
            Orders order = ordersService.getByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在，跳过超时取消: {}", orderNo);
            } else if (order.getStatus() != OrderStatus.UNPAID) {
                log.info("订单状态非未支付({})，跳过超时取消 orderNo={}", order.getStatus(), orderNo);
            } else {
                ordersService.cancelOrder(orderNo);
            }
            // 手动 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单超时取消失败: {}", orderNo, e);
            try {
                // 拒绝消息不重回队列，防止死循环
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("死信消息NACK失败: {}", orderNo, ex);
            }
        }
    }
}
