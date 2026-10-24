package com.gym.search.controller;

import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.search.service.SearchService;
import com.gym.search.vo.GoodsDocVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Api(tags = "搜索接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @ApiOperation("商品全文搜索(高亮分页)")
    @GetMapping("/goods")
    public R<PageResult<GoodsDocVO>> searchGoods(
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @ApiParam("最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @ApiParam("页码(从1开始)") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("排序: _score(默认)/sales(销量降序)/priceAsc/priceDesc")
            @RequestParam(defaultValue = "_score") String sort) {
        return R.ok(searchService.searchGoods(keyword, categoryId, minPrice, maxPrice, pageNum, pageSize, sort));
    }

    @ApiOperation("商品名称自动补全")
    @GetMapping("/suggest")
    public R<List<String>> suggest(
            @ApiParam("前缀") @RequestParam String prefix,
            @ApiParam("返回数量") @RequestParam(defaultValue = "10") int size) {
        return R.ok(searchService.suggest(prefix, size));
    }
}
