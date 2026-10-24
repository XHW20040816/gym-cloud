package com.gym.course.controller.internal;

import com.gym.common.result.R;
import com.gym.course.mapper.CourseScheduleMapper;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "内部接口", hidden = true)
@RestController
@RequestMapping("/course/internal")
public class InternalCourseController {

    @Resource
    private CourseScheduleMapper scheduleMapper;

    @PostMapping("/schedule/enrollment")
    public R<Void> addEnrollment(@RequestParam Long scheduleId,
                                 @RequestParam(defaultValue = "1") Integer count) {
        if (count == null || count <= 0) {
            count = 1;
        }
        scheduleMapper.addEnrollment(scheduleId, count);
        return R.ok();
    }
}
