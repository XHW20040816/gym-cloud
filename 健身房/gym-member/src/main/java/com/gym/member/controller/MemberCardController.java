package com.gym.member.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.member.MemberCard;
import com.gym.member.service.MemberCardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "会员卡管理")
@RestController
@RequestMapping("/member/card")
public class MemberCardController {

    @Resource
    private MemberCardService memberCardService;

    @ApiOperation("会员卡分页查询")
    @GetMapping("/page")
    public R<PageResult<MemberCard>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                          @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                          @ApiParam("卡名模糊") @RequestParam(required = false) String nameLike) {
        Page<MemberCard> page = new Page<>(pageNum, pageSize);
        IPage<MemberCard> result = memberCardService.listPage(page, nameLike);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取所有会员卡列表（公开）")
    @GetMapping("/list")
    public R<PageResult<MemberCard>> list() {
        Page<MemberCard> page = new Page<>(1, 1000);
        IPage<MemberCard> result = memberCardService.listPage(page, null);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取单个会员卡详情")
    @GetMapping("/{id}")
    public R<MemberCard> getById(@PathVariable Long id) {
        MemberCard card = memberCardService.getById(id);
        return R.ok(card);
    }

    @ApiOperation("新增会员卡（ADMIN）")
    @PostMapping
    public R<Void> save(@RequestBody MemberCard card, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        memberCardService.save(card);
        return R.ok();
    }

    @ApiOperation("修改会员卡（ADMIN）")
    @PutMapping
    public R<Void> update(@RequestBody MemberCard card, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        memberCardService.updateById(card);
        return R.ok();
    }

    @ApiOperation("删除会员卡（ADMIN）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        memberCardService.removeById(id);
        return R.ok();
    }
}
