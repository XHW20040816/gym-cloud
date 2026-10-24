package com.gym.member.controller.internal;

import com.gym.common.result.R;
import com.gym.member.service.UserMemberService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "内部接口", hidden = true)
@RestController
@RequestMapping("/member/internal")
public class InternalMemberController {

    @Resource
    private UserMemberService userMemberService;

    @PostMapping("/open")
    public R<Void> open(@RequestParam Long userId,
                        @RequestParam Long cardId) {
        userMemberService.saveUserMember(userId, cardId);
        return R.ok();
    }

    @PostMapping("/deductTimes")
    public R<Void> deductTimes(@RequestParam Long userMemberId,
                               @RequestParam Integer times) {
        userMemberService.deductTimes(userMemberId, times);
        return R.ok();
    }
}
