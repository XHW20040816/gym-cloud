package com.gym.search.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gym.entity.goods.Goods;
import com.gym.search.document.GoodsDoc;
import com.gym.search.mapper.GoodsMapper;
import com.gym.search.repository.GoodsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class GoodsFullSyncRunner implements CommandLineRunner {

    private final GoodsMapper goodsMapper;
    private final GoodsRepository goodsRepository;

    @Override
    public void run(String... args) {
        log.info("开始 MySQL→ES 全量同步...");
        List<Goods> goods = goodsMapper.selectList(Wrappers.lambdaQuery());
        if (CollUtil.isEmpty(goods)) {
            log.info("商品表为空，跳过全量同步");
            return;
        }
        List<GoodsDoc> docs = goods.stream().map(this::toDoc).collect(Collectors.toList());
        goodsRepository.saveAll(docs);
        log.info("ES 全量同步完成，共 {} 条", docs.size());
    }

    private GoodsDoc toDoc(Goods g) {
        GoodsDoc d = new GoodsDoc();
        BeanUtil.copyProperties(g, d);
        Completion completion = new Completion(new String[]{g.getName()});
        completion.setWeight(g.getSales() == null ? 1 : g.getSales() + 1);
        d.setSuggest(completion);
        return d;
    }
}
