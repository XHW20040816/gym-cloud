package com.gym.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.gym.common.result.PageResult;
import com.gym.search.document.GoodsDoc;
import com.gym.search.service.SearchService;
import com.gym.search.vo.GoodsDocVO;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchRestTemplate esTemplate;

    @Override
    public PageResult<GoodsDocVO> searchGoods(String keyword, Long categoryId, BigDecimal minP,
                                              BigDecimal maxP, Integer pageNum, Integer pageSize, String sort) {
        // 1) BoolQueryBuilder
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        if (StrUtil.isNotBlank(keyword)) {
            // 字段权重必须用 field(name, boost) 设置；"name^3" 这种字符串写法会被 ES 当作字面字段名，
            // 7.17 会直接抛 number_format_exception（For input string: "3^1.0"）
            MultiMatchQueryBuilder mm = QueryBuilders.multiMatchQuery(keyword)
                    .field("name", 3.0f)
                    .field("description")
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS);
            bool.must(mm);
        }
        if (categoryId != null) {
            bool.filter(QueryBuilders.termQuery("categoryId", categoryId));
        }
        if (minP != null || maxP != null) {
            RangeQueryBuilder rqb = QueryBuilders.rangeQuery("price");
            if (minP != null) {
                rqb.gte(minP.doubleValue());
            }
            if (maxP != null) {
                rqb.lte(maxP.doubleValue());
            }
            bool.filter(rqb);
        }
        bool.filter(QueryBuilders.termQuery("status", 1));

        // 2) Highlight name + description
        HighlightBuilder hb = new HighlightBuilder()
                .field("name", 20, 3)
                .field("description", 50, 5)
                .preTags("<em style=\"color:#F56C6C\">")
                .postTags("</em>");

        // 3) Sort & Page
        NativeSearchQueryBuilder qb = new NativeSearchQueryBuilder()
                .withQuery(bool)
                .withHighlightBuilder(hb);
        if ("sales".equals(sort)) {
            qb.withSort(Sort.by(Sort.Direction.DESC, "sales"));
        } else if ("priceAsc".equals(sort)) {
            qb.withSort(Sort.by(Sort.Direction.ASC, "price"));
        } else if ("priceDesc".equals(sort)) {
            qb.withSort(Sort.by(Sort.Direction.DESC, "price"));
        }
        qb.withPageable(PageRequest.of(pageNum - 1, pageSize));

        // 4) queryForPage
        SearchHits<GoodsDoc> hits = esTemplate.search(qb.build(), GoodsDoc.class);
        List<GoodsDocVO> list = new ArrayList<>();
        for (SearchHit<GoodsDoc> hit : hits) {
            GoodsDocVO vo = new GoodsDocVO();
            BeanUtil.copyProperties(hit.getContent(), vo);
            List<String> hName = hit.getHighlightFields().get("name");
            if (CollUtil.isNotEmpty(hName)) {
                vo.setName(hName.get(0));
            }
            List<String> hDesc = hit.getHighlightFields().get("description");
            if (CollUtil.isNotEmpty(hDesc)) {
                vo.setDescription(hDesc.get(0));
            }
            list.add(vo);
        }
        return PageResult.of(list, hits.getTotalHits());
    }

    @Override
    public List<String> suggest(String prefix, int size) {
        CompletionSuggestionBuilder sb = SuggestBuilders
                .completionSuggestion("suggest")
                .prefix(prefix, Fuzziness.AUTO)
                .size(size)
                .skipDuplicates(true);
        SuggestBuilder suggestBuilder = new SuggestBuilder()
                .addSuggestion("goods_suggest", sb);
        SearchResponse resp = esTemplate.execute(client -> client.search(
                new SearchRequest().indices("goods_index")
                        .source(new SearchSourceBuilder().suggest(suggestBuilder)),
                RequestOptions.DEFAULT
        ));
        CompletionSuggestion suggest = resp.getSuggest().getSuggestion("goods_suggest");
        return suggest.getEntries().stream()
                .flatMap(e -> e.getOptions().stream())
                .map(o -> o.getText().string())
                .distinct()
                .collect(Collectors.toList());
    }
}
