package com.gym.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.course.mapper.CourseMapper;
import com.gym.course.service.CourseEnrollmentService;
import com.gym.course.service.CourseScheduleService;
import com.gym.course.service.CourseService;
import com.gym.entity.course.Course;
import com.gym.entity.course.CourseEnrollment;
import com.gym.entity.course.CourseSchedule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseScheduleService scheduleService;

    @Resource
    private CourseEnrollmentService enrollmentService;

    @Override
    public IPage<Course> listPage(Page<Course> page, String name, String category, Long coachId, Integer status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Course::getName, name);
        }
        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(Course::getCategory, category);
        }
        if (coachId != null) {
            wrapper.eq(Course::getCoachId, coachId);
        }
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return courseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithSchedules(Long id) {
        // ① 查该课程下所有排期
        List<CourseSchedule> schedules = scheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>().eq(CourseSchedule::getCourseId, id));
        List<Long> scheduleIds = schedules.stream().map(CourseSchedule::getId).collect(Collectors.toList());
        if (!scheduleIds.isEmpty()) {
            // ② 级联删除报名
            enrollmentService.remove(
                    new LambdaQueryWrapper<CourseEnrollment>().in(CourseEnrollment::getScheduleId, scheduleIds));
            // ③ 级联删除排期
            scheduleService.removeByIds(scheduleIds);
        }
        // ④ 删除课程
        removeById(id);
    }
}
