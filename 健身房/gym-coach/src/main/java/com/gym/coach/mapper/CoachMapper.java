package com.gym.coach.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.course.CourseSchedule;
import com.gym.entity.user.Coach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoachMapper extends BaseMapper<Coach> {

    /**
     * 查询指定教练的所有课程排期（JOIN Course 限定 coachId）
     */
    @Select("SELECT DISTINCT cs.* FROM t_course_schedule cs " +
            "INNER JOIN t_course c ON cs.course_id = c.id " +
            "WHERE c.coach_id = #{coachId} " +
            "ORDER BY cs.class_date DESC, cs.start_time ASC")
    List<CourseSchedule> listScheduleByCoachId(Long coachId);
}
