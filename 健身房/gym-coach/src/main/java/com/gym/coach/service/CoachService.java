package com.gym.coach.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.course.CourseSchedule;
import com.gym.entity.user.Coach;

import java.util.List;

public interface CoachService extends IService<Coach> {

    IPage<Coach> listPage(Page<Coach> page, String realNameLike, String specialtyLike);

    List<CourseSchedule> listSchedulesByCoachId(Long coachId);
}
