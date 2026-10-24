package com.gym.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.course.CourseEnrollment;

public interface CourseEnrollmentService extends IService<CourseEnrollment> {

    /**
     * 严格容量控制预约报名（AC-7）
     * @param scheduleId 排期ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean enroll(Long scheduleId, Long userId);

    IPage<CourseEnrollment> listPage(Page<CourseEnrollment> page, Long scheduleId, Long userId, Integer status);
}
