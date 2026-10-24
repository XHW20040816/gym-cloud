package com.gym.search.repository;

import com.gym.search.document.GoodsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsRepository extends ElasticsearchRepository<GoodsDoc, Long> {
}
