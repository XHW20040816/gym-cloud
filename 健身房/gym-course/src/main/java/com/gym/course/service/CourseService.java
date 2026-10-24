package com.gym.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.course.Course;

public interface CourseService extends IService<Course> {

    IPage<Course> listPage(Page<Course> page, String name, String category, Long coachId, Integer status);

    /** 删除课程并级联删除其排期与报名 */
    void deleteWithSchedules(Long id);
}
