package com.gym.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.goods.Goods;
import com.gym.goods.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    @ApiOperation("商品分页查询（公开）")
    @GetMapping("/page")
    public R<PageResult<Goods>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                     @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                     @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
                                     @ApiParam("商品名模糊") @RequestParam(required = false) String nameLike,
                                     @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<Goods> page = new Page<>(pageNum, pageSize);
        IPage<Goods> result = goodsService.listPage(page, categoryId, nameLike, status);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取单个商品详情（公开）")
    @GetMapping("/{id}")
    public R<Goods> getById(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        return R.ok(goods);
    }

    @ApiOperation("新增商品（ADMIN）")
    @PostMapping
    public R<Void> save(@RequestBody Goods goods, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsService.saveOrUpdateGoods(goods);
        return R.ok();
    }

    @ApiOperation("修改商品（ADMIN）")
    @PutMapping
    public R<Void> update(@RequestBody Goods goods, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsService.saveOrUpdateGoods(goods);
        return R.ok();
    }

    @ApiOperation("删除商品（ADMIN）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsService.removeGoods(id);
        return R.ok();
    }

    @ApiOperation("商品上下架（ADMIN）")
    @PutMapping("/status")
    public R<Void> updateStatus(@ApiParam("商品ID") @RequestParam Long id,
                                @ApiParam("状态：1上架 0下架") @RequestParam Integer status,
                                HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsService.updateStatus(id, status);
        return R.ok();
    }
}
