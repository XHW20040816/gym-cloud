package com.gym.search.service;

import com.gym.common.result.PageResult;
import com.gym.search.vo.GoodsDocVO;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {

    /**
     * 关键词搜索 + 高亮 + 分页
     */
    PageResult<GoodsDocVO> searchGoods(String keyword, Long categoryId, BigDecimal minPrice,
                                       BigDecimal maxPrice, Integer pageNum, Integer pageSize, String sort);

    /**
     * 自动补全 suggest
     */
    List<String> suggest(String prefix, int size);
}
