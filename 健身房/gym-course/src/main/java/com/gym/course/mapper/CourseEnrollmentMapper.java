package com.gym.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.course.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {
}
