package com.gym.coach.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.coach.service.CoachService;
import com.gym.entity.course.CourseSchedule;
import com.gym.entity.user.Coach;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "教练管理")
@RestController
@RequestMapping("/coach")
public class CoachController {

    @Resource
    private CoachService coachService;

    @ApiOperation("教练分页查询（公开）")
    @GetMapping("/page")
    public R<PageResult<Coach>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                     @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                     @ApiParam("姓名模糊") @RequestParam(required = false) String realNameLike,
                                     @ApiParam("擅长领域模糊") @RequestParam(required = false) String specialtyLike) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        IPage<Coach> result = coachService.listPage(page, realNameLike, specialtyLike);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取教练详情（公开）")
    @GetMapping("/{id}")
    public R<Coach> getById(@PathVariable Long id) {
        Coach coach = coachService.getById(id);
        return R.ok(coach);
    }

    @ApiOperation("查询教练的所有课程排期（公开）")
    @GetMapping("/{id}/schedule")
    public R<List<CourseSchedule>> getScheduleByCoachId(@PathVariable Long id) {
        List<CourseSchedule> list = coachService.listSchedulesByCoachId(id);
        return R.ok(list);
    }

    @ApiOperation("新增教练（ADMIN）")
    @PostMapping
    public R<Void> save(@RequestBody Coach coach, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        coachService.save(coach);
        return R.ok();
    }

    @ApiOperation("修改教练（ADMIN）")
    @PutMapping
    public R<Void> update(@RequestBody Coach coach, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        coachService.updateById(coach);
        return R.ok();
    }

    @ApiOperation("删除教练（ADMIN）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        coachService.removeById(id);
        return R.ok();
    }
}
