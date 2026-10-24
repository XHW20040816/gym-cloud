package com.gym.seckill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.seckill.SeckillActivity;
import com.gym.seckill.service.SeckillActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "后台-秒杀活动管理")
@RestController
@RequestMapping("/seckill/admin/activity")
public class SeckillActivityController {

    @Resource
    private SeckillActivityService seckillActivityService;

    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getHeader("X-User-Role"));
    }

    @ApiOperation("活动分页")
    @GetMapping("/page")
    public R<PageResult<SeckillActivity>> page(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态：0未开始 1进行中 2已结束") @RequestParam(required = false) Integer status,
            @ApiParam("活动名称模糊") @RequestParam(required = false) String name,
            HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        IPage<SeckillActivity> result = seckillActivityService.listPage(page, status, name);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("活动详情")
    @GetMapping("/{id}")
    public R<SeckillActivity> detail(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        return R.ok(seckillActivityService.getById(id));
    }

    @ApiOperation("新增活动")
    @PostMapping
    public R<Void> save(@RequestBody SeckillActivity activity, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        seckillActivityService.save(activity);
        return R.ok();
    }

    @ApiOperation("修改活动")
    @PutMapping
    public R<Void> update(@RequestBody SeckillActivity activity, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        seckillActivityService.updateById(activity);
        return R.ok();
    }

    @ApiOperation("删除活动")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        seckillActivityService.deleteWithGoods(id);
        return R.ok();
    }

    @ApiOperation("修改活动状态：0未开始 1进行中 2已结束")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                @RequestParam Integer status,
                                HttpServletRequest request) {
        if (!isAdmin(request)) return R.fail(ResultCode.FORBIDDEN);
        SeckillActivity activity = new SeckillActivity();
        activity.setId(id);
        activity.setStatus(status);
        seckillActivityService.updateById(activity);
        return R.ok();
    }
}
