package com.gym.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.course.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {

    /**
     * 原子性 +1 并检查容量，返回影响行数
     */
    @Update("UPDATE t_course_schedule SET enrollment_count = enrollment_count + 1 " +
            "WHERE id = #{id} AND enrollment_count < capacity")
    int incrementEnrollment(Long id);

    /**
     * 内部接口：直接增加报名人数（不检查容量，配合内部调用）
     */
    @Update("UPDATE t_course_schedule SET enrollment_count = enrollment_count + #{count} WHERE id = #{id}")
    int addEnrollment(@Param("id") Long id, @Param("count") Integer count);
}
