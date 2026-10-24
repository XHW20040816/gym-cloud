package com.gym.user.controller.internal;

import com.gym.common.result.R;
import com.gym.entity.user.User;
import com.gym.user.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * Feign 内部调用接口，不在 Swagger 文档显示（通过 @Api(hidden=true) 隐藏）
 */
@Api(tags = "内部接口", hidden = true)
@RestController
@RequestMapping("/user/internal")
public class InternalUserController {

    @Resource
    private UserService userService;

    @GetMapping("/{id}")
    public R<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return R.ok(user);
    }

    @GetMapping("/username/{username}")
    public R<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return R.ok(user);
    }

    @PostMapping("/deductBalance")
    public R<Void> deductBalance(@RequestParam Long userId,
                                 @RequestParam BigDecimal amount) {
        userService.deductBalance(userId, amount);
        return R.ok();
    }
}
