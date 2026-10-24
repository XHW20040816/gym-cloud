package com.gym.goods.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.goods.GoodsCategory;
import com.gym.goods.service.GoodsCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "商品分类管理")
@RestController
@RequestMapping("/goods/category")
public class GoodsCategoryController {

    @Resource
    private GoodsCategoryService goodsCategoryService;

    @ApiOperation("分类分页查询")
    @GetMapping("/page")
    public R<PageResult<GoodsCategory>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                             @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                             @ApiParam("分类名模糊查询") @RequestParam(required = false) String nameLike,
                                             HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        Page<GoodsCategory> page = new Page<>(pageNum, pageSize);
        IPage<GoodsCategory> result = goodsCategoryService.listPage(page, nameLike);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取所有分类列表（公开）")
    @GetMapping("/list")
    public R<PageResult<GoodsCategory>> list() {
        Page<GoodsCategory> page = new Page<>(1, 1000);
        IPage<GoodsCategory> result = goodsCategoryService.listPage(page, null);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取单个分类详情")
    @GetMapping("/{id}")
    public R<GoodsCategory> getById(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        GoodsCategory category = goodsCategoryService.getById(id);
        return R.ok(category);
    }

    @ApiOperation("新增分类")
    @PostMapping
    public R<Void> save(@RequestBody GoodsCategory category, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsCategoryService.save(category);
        return R.ok();
    }

    @ApiOperation("修改分类")
    @PutMapping
    public R<Void> update(@RequestBody GoodsCategory category, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsCategoryService.updateById(category);
        return R.ok();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        goodsCategoryService.removeById(id);
        return R.ok();
    }
}
