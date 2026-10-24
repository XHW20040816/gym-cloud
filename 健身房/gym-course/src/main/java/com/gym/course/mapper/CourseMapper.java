package com.gym.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.course.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
