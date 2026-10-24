package com.gym.food.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.food.Food;
import com.gym.food.service.FoodService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "餐饮管理")
@RestController
@RequestMapping("/food")
public class FoodController {

    @Resource
    private FoodService foodService;

    @ApiOperation("餐饮列表（公开可查，默认只查上架）")
    @GetMapping("/list")
    public R<List<Food>> list(@ApiParam("状态：1上架 0下架，不传查全部") @RequestParam(required = false) Integer status) {
        if (status == null) {
            status = 1;
        }
        List<Food> list = foodService.listAll(status);
        return R.ok(list);
    }

    @ApiOperation("餐饮分页查询（ADMIN）")
    @GetMapping("/page")
    public R<PageResult<Food>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                    @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                    @ApiParam("餐品名模糊") @RequestParam(required = false) String nameLike,
                                    @ApiParam("状态") @RequestParam(required = false) Integer status,
                                    HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        Page<Food> page = new Page<>(pageNum, pageSize);
        IPage<Food> result = foodService.listPage(page, nameLike, status);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取单个餐饮详情")
    @GetMapping("/{id}")
    public R<Food> getById(@PathVariable Long id) {
        Food food = foodService.getById(id);
        return R.ok(food);
    }

    @ApiOperation("新增餐饮（ADMIN）")
    @PostMapping
    public R<Void> save(@RequestBody Food food, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        foodService.save(food);
        return R.ok();
    }

    @ApiOperation("修改餐饮（ADMIN）")
    @PutMapping
    public R<Void> update(@RequestBody Food food, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        foodService.updateById(food);
        return R.ok();
    }

    @ApiOperation("删除餐饮（ADMIN）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        foodService.removeById(id);
        return R.ok();
    }
}
