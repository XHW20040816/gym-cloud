package com.gym.search.runner;

import com.gym.search.document.GoodsDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
public class IndexInitRunner implements ApplicationRunner {

    private final ElasticsearchRestTemplate esTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        doInit();
    }

    private void doInit() {
        IndexOperations io = esTemplate.indexOps(GoodsDoc.class);
        if (!io.exists()) {
            io.create();
            Document mapping = io.createMapping(GoodsDoc.class);
            io.putMapping(mapping);
            log.info("ES 索引 goods_index 创建完成并已写入 mapping");
        } else {
            log.info("ES 索引 goods_index 已存在");
        }
    }
}
