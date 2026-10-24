package com.gym.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.course.service.CourseEnrollmentService;
import com.gym.course.service.CourseScheduleService;
import com.gym.entity.course.CourseSchedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@Api(tags = "课程排期与预约")
@RestController
@RequestMapping("/course/schedule")
public class ScheduleController {

    @Resource
    private CourseScheduleService scheduleService;

    @Resource
    private CourseEnrollmentService enrollmentService;

    @ApiOperation("排充分页查询（公开）")
    @GetMapping("/page")
    public R<PageResult<CourseSchedule>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                              @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                              @ApiParam("教练ID") @RequestParam(required = false) Long coachId,
                                              @ApiParam("课程ID") @RequestParam(required = false) Long courseId,
                                              @ApiParam("上课日期起") @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                              @ApiParam("上课日期止") @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        IPage<CourseSchedule> result = scheduleService.listPage(page, coachId, courseId, startDate, endDate);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取排期详情（公开）")
    @GetMapping("/{id}")
    public R<CourseSchedule> getById(@PathVariable Long id) {
        CourseSchedule schedule = scheduleService.getById(id);
        return R.ok(schedule);
    }

    @ApiOperation("新增排期（ADMIN）")
    @PostMapping
    public R<Void> save(@RequestBody CourseSchedule schedule, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        scheduleService.saveSchedule(schedule);
        return R.ok();
    }

    @ApiOperation("修改排期（ADMIN）")
    @PutMapping
    public R<Void> update(@RequestBody CourseSchedule schedule, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        scheduleService.updateSchedule(schedule);
        return R.ok();
    }

    @ApiOperation("删除排期（ADMIN）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        scheduleService.removeById(id);
        return R.ok();
    }

    @ApiOperation("用户预约课程（容量控制 AC-7）")
    @PostMapping("/enroll")
    public R<Void> enroll(@ApiParam("排期ID") @RequestParam Long scheduleId,
                          HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        enrollmentService.enroll(scheduleId, userId);
        return R.ok();
    }
}
