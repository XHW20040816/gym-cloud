package com.gym.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import com.gym.course.mapper.CourseEnrollmentMapper;
import com.gym.course.mapper.CourseScheduleMapper;
import com.gym.course.service.CourseEnrollmentService;
import com.gym.entity.course.CourseEnrollment;
import com.gym.entity.course.CourseSchedule;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> implements CourseEnrollmentService {

    @Resource
    private CourseEnrollmentMapper enrollmentMapper;

    @Resource
    private CourseScheduleMapper scheduleMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enroll(Long scheduleId, Long userId) {
        // ① 查排期判空，capacity == enrollment_count -> 课程已满
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BizException("课程排期不存在");
        }
        if (schedule.getCapacity() != null && schedule.getEnrollmentCount() != null
                && schedule.getCapacity().equals(schedule.getEnrollmentCount())) {
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH.getCode(), "课程已满");
        }

        // ② Redis 短期防重 key：只用于拦截同一瞬间的并发重复提交，
        //    真正的「一人一场」由 DB 唯一键 uk_schedule_user 保证；
        //    key 在请求结束（finally）即删除，避免取消预约后在 TTL 内无法重新预约
        String lockKey = "course:enroll:" + scheduleId + ":" + userId;
        Boolean absent = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        if (absent == null || !absent) {
            throw new BizException(ResultCode.REPEAT_SECKILL.getCode(), "您已预约");
        }

        try {
            // ③ 原子 SQL 增加报名人数并检查容量
            int rows = scheduleMapper.incrementEnrollment(scheduleId);
            if (rows == 0) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH.getCode(), "课程已满");
            }

            // ④ 插入报名记录（schedule_id + user_id 唯一，重复报名在此抛 DuplicateKeyException）
            CourseEnrollment enroll = new CourseEnrollment();
            enroll.setScheduleId(scheduleId);
            enroll.setUserId(userId);
            enroll.setEnrollTime(LocalDateTime.now());
            enroll.setStatus(1);
            enrollmentMapper.insert(enroll);

            return true;
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.REPEAT_SECKILL.getCode(), "您已预约");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("预约失败：" + e.getMessage());
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    public IPage<CourseEnrollment> listPage(Page<CourseEnrollment> page, Long scheduleId, Long userId, Integer status) {
        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<>();
        if (scheduleId != null) {
            wrapper.eq(CourseEnrollment::getScheduleId, scheduleId);
        }
        if (userId != null) {
            wrapper.eq(CourseEnrollment::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(CourseEnrollment::getStatus, status);
        }
        wrapper.orderByDesc(CourseEnrollment::getEnrollTime);
        return enrollmentMapper.selectPage(page, wrapper);
    }
}
