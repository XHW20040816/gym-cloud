package com.gym.search.mq;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gym.common.constant.MQConstants;
import com.gym.entity.goods.Goods;
import com.gym.search.document.GoodsDoc;
import com.gym.search.mapper.GoodsMapper;
import com.gym.search.repository.GoodsRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = MQConstants.GOODS_QUEUE)
public class GoodsSyncConsumer {

    private final GoodsMapper goodsMapper;
    private final GoodsRepository goodsRepository;

    @RabbitHandler
    public void onMessage(String jsonStr, Channel channel, Message message) {
        log.info("收到商品ES同步消息: {}", jsonStr);
        try {
            JSONObject msg = JSON.parseObject(jsonStr);
            Long goodsId = msg.getLong("id");
            String action = msg.getString("action");
            if (goodsId == null) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            if ("D".equals(action)) {
                goodsRepository.deleteById(goodsId);
                log.info("ES 删除商品 id={}", goodsId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            Goods dbGoods = goodsMapper.selectById(goodsId);
            if (dbGoods == null) {
                goodsRepository.deleteById(goodsId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            GoodsDoc doc = convert(dbGoods);
            goodsRepository.save(doc);
            log.info("ES 同步商品 {} 完成", dbGoods.getName());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("ES 商品同步失败: {}", jsonStr, e);
            try {
                // 同步失败不重回队列，避免循环消费（ES 同步失败可由下次商品变更重新触发）
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("ES同步消息NACK失败", ex);
            }
        }
    }

    private GoodsDoc convert(Goods g) {
        GoodsDoc d = new GoodsDoc();
        BeanUtil.copyProperties(g, d);
        Completion completion = new Completion(new String[]{g.getName()});
        completion.setWeight(g.getSales() == null ? 1 : g.getSales() + 1);
        d.setSuggest(completion);
        return d;
    }
}
