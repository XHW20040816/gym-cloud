package com.gym.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.course.CourseSchedule;

import java.time.LocalDate;

public interface CourseScheduleService extends IService<CourseSchedule> {

    IPage<CourseSchedule> listPage(Page<CourseSchedule> page, Long coachId, Long courseId,
                                   LocalDate startDate, LocalDate endDate);

    boolean saveSchedule(CourseSchedule schedule);

    boolean updateSchedule(CourseSchedule schedule);
}
