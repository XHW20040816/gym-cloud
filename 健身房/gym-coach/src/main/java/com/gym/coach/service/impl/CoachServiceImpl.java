package com.gym.coach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.coach.mapper.CoachMapper;
import com.gym.coach.service.CoachService;
import com.gym.entity.course.CourseSchedule;
import com.gym.entity.user.Coach;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CoachServiceImpl extends ServiceImpl<CoachMapper, Coach> implements CoachService {

    @Resource
    private CoachMapper coachMapper;

    @Override
    public IPage<Coach> listPage(Page<Coach> page, String realNameLike, String specialtyLike) {
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(realNameLike)) {
            wrapper.like(Coach::getRealName, realNameLike);
        }
        if (StringUtils.isNotBlank(specialtyLike)) {
            wrapper.like(Coach::getSpecialty, specialtyLike);
        }
        wrapper.orderByDesc(Coach::getRating);
        return coachMapper.selectPage(page, wrapper);
    }

    @Override
    public List<CourseSchedule> listSchedulesByCoachId(Long coachId) {
        return coachMapper.listScheduleByCoachId(coachId);
    }
}
