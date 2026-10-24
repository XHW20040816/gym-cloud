package com.gym.seckill.controller;

import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.seckill.service.SeckillGoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "后台-秒杀商品管理")
@RestController
@RequestMapping("/seckill/admin/goods")
public class SeckillGoodsController {

    @Resource
    private SeckillGoodsService seckillGoodsService;

    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getHeader("X-User-Role"));
    }

    @ApiOperation("按活动查秒杀商品")
    @GetMapping("/list/{activityId}")
    public R<List<SeckillGoods>> list(@PathVariable Long activityId, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        return R.ok(seckillGoodsService.listByActivityId(activityId));
    }

    @ApiOperation("秒杀商品详情")
    @GetMapping("/{id}")
    public R<SeckillGoods> detail(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        return R.ok(seckillGoodsService.getById(id));
    }

    @ApiOperation("新增秒杀商品")
    @PostMapping
    public R<Void> save(@RequestBody SeckillGoods goods, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        seckillGoodsService.save(goods);
        return R.ok();
    }

    @ApiOperation("修改秒杀商品")
    @PutMapping
    public R<Void> update(@RequestBody SeckillGoods goods, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        seckillGoodsService.updateById(goods);
        return R.ok();
    }

    @ApiOperation("删除秒杀商品")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        seckillGoodsService.removeById(id);
        return R.ok();
    }
}
