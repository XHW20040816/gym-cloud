package com.gym.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.course.mapper.CourseScheduleMapper;
import com.gym.course.service.CourseScheduleService;
import com.gym.entity.course.CourseSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;

@Service
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {

    @Resource
    private CourseScheduleMapper scheduleMapper;

    @Override
    public IPage<CourseSchedule> listPage(Page<CourseSchedule> page, Long coachId, Long courseId,
                                          LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(CourseSchedule::getCourseId, courseId);
        }
        if (startDate != null) {
            wrapper.ge(CourseSchedule::getClassDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(CourseSchedule::getClassDate, endDate);
        }
        // coachId 查询时需要 JOIN，这里只按 schedule 自身字段过滤；
        // 如果传入 coachId，使用子查询限定 course_id 在该教练课程内
        if (coachId != null) {
            wrapper.inSql(CourseSchedule::getCourseId,
                    "SELECT id FROM t_course WHERE coach_id = " + coachId);
        }
        wrapper.orderByAsc(CourseSchedule::getClassDate, CourseSchedule::getStartTime);
        return scheduleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSchedule(CourseSchedule schedule) {
        if (schedule.getEnrollmentCount() == null) {
            schedule.setEnrollmentCount(0);
        }
        return super.save(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(CourseSchedule schedule) {
        return super.updateById(schedule);
    }
}
