package com.gym.api.course;

import com.gym.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gym-course")
public interface CourseFeignClient {

    @PostMapping("/course/internal/schedule/enrollment")
    R<Void> addEnrollment(@RequestParam("scheduleId") Long scheduleId,
                          @RequestParam("count") Integer count);
}
