package com.gym.user.controller.internal;

import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.RoleType;
import com.gym.entity.enums.UserStatus;
import com.gym.entity.user.User;
import com.gym.user.service.UserService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

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

    @PostMapping("/listByIds")
    public R<List<User>> getUserByIds(@RequestBody List<Long> ids) {
        return R.ok(userService.listByIds(ids));
    }

    @PostMapping("/deductBalance")
    public R<Void> deductBalance(@RequestParam Long userId,
                                 @RequestParam BigDecimal amount) {
        userService.deductBalance(userId, amount);
        return R.ok();
    }

    @PostMapping("/addBalance")
    public R<Void> addBalance(@RequestParam Long userId,
                              @RequestParam BigDecimal amount) {
        userService.addBalance(userId, amount);
        return R.ok();
    }

    @PostMapping("/register")
    public R<Long> register(@RequestBody User user) {
        if (user == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return R.fail(ResultCode.PARAM_ERROR, "用户名和密码不能为空");
        }
        if (userService.getByUsername(user.getUsername()) != null) {
            return R.fail(ResultCode.PARAM_ERROR, "用户名已存在");
        }
        user.setId(null);
        // 角色：默认普通用户，仅允许三种合法角色
        RoleType role = user.getRole();
        if (role == null) {
            role = RoleType.USER;
        } else if (role != RoleType.USER && role != RoleType.COACH && role != RoleType.ADMIN) {
            return R.fail(ResultCode.PARAM_ERROR, "非法的角色类型");
        }
        user.setRole(role);
        user.setBalance(BigDecimal.ZERO);
        user.setPoint(0);
        user.setStatus(UserStatus.NORMAL);
        userService.saveUser(user);
        return R.ok(user.getId());
    }
}
