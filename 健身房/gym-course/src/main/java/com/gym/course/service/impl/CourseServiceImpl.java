package com.gym.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.course.mapper.CourseMapper;
import com.gym.course.service.CourseService;
import com.gym.entity.course.Course;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Resource
    private CourseMapper courseMapper;

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
}
