package com.gym.course.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.course.service.CourseService;
import com.gym.entity.course.Course;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "课程管理")
@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    @ApiOperation("课程分页查询（公开）")
    @GetMapping("/page")
    public R<PageResult<Course>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                      @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                      @ApiParam("课程名") @RequestParam(required = false) String name,
                                      @ApiParam("分类") @RequestParam(required = false) String category,
                                      @ApiParam("教练ID") @RequestParam(required = false) Long coachId,
                                      @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        IPage<Course> result = courseService.listPage(page, name, category, coachId, status);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("获取课程详情（公开）")
    @GetMapping("/{id}")
    public R<Course> getById(@PathVariable Long id) {
        Course course = courseService.getById(id);
        return R.ok(course);
    }

    @ApiOperation("新增课程（ADMIN）")
    @PostMapping
    public R<Void> save(@RequestBody Course course, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        courseService.save(course);
        return R.ok();
    }

    @ApiOperation("修改课程（ADMIN）")
    @PutMapping
    public R<Void> update(@RequestBody Course course, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        courseService.updateById(course);
        return R.ok();
    }

    @ApiOperation("删除课程（ADMIN）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        courseService.deleteWithSchedules(id);
        return R.ok();
    }

    @ApiOperation("课程上下架（ADMIN）")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@ApiParam("课程ID") @PathVariable Long id,
                                @ApiParam("状态：1启用 0禁用") @RequestParam Integer status,
                                HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        courseService.updateById(course);
        return R.ok();
    }
}
