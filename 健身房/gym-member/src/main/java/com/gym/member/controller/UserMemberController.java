package com.gym.member.controller;

import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.member.UserMember;
import com.gym.member.service.UserMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "用户会员开通")
@RestController
@RequestMapping("/member")
public class UserMemberController {

    @Resource
    private UserMemberService userMemberService;

    @ApiOperation("查询当前用户的会员卡列表")
    @GetMapping("/user/myCards")
    public R<List<UserMember>> myCards(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        List<UserMember> list = userMemberService.listByUserId(userId);
        return R.ok(list);
    }

    @ApiOperation("开通会员卡（普通API场景，不扣余额，后续走支付流程）")
    @PostMapping("/open")
    public R<Void> openCard(@ApiParam("会员卡ID") @RequestParam Long cardId,
                            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        userMemberService.openCard(userId, cardId);
        return R.ok();
    }
}
