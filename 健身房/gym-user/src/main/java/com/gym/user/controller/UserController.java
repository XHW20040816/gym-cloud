package com.gym.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.user.User;
import com.gym.user.dto.UserDTO;
import com.gym.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/info")
    public R<User> info(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // 脱敏
        }
        return R.ok(user);
    }

    @ApiOperation("用户分页查询")
    @GetMapping("/page")
    public R<PageResult<User>> page(@ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                    @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
                                    UserDTO dto,
                                    HttpServletRequest request) {
        // 鉴权：非 ADMIN 403
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = userService.listPage(page, dto);
        // 脱敏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("保存用户")
    @PostMapping
    public R<Void> save(@RequestBody User user, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        userService.saveUser(user);
        return R.ok();
    }

    @ApiOperation("修改用户")
    @PutMapping
    public R<Void> update(@RequestBody User user, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            // 普通用户只能改自己的信息
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr == null || !userIdStr.equals(String.valueOf(user.getId()))) {
                return R.fail(ResultCode.FORBIDDEN);
            }
            // 白名单：仅允许改基础资料，防止提权/篡改余额积分状态
            User safe = new User();
            safe.setId(user.getId());
            safe.setNickname(user.getNickname());
            safe.setPhone(user.getPhone());
            safe.setEmail(user.getEmail());
            safe.setAvatar(user.getAvatar());
            user = safe;
        }
        userService.updateUser(user);
        return R.ok();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        userService.removeById(id);
        return R.ok();
    }

    @ApiOperation("修改用户启停用状态")
    @PutMapping("/status")
    public R<Void> updateStatus(@ApiParam("用户ID") @RequestParam Long userId,
                                @ApiParam("状态：1启用 0禁用") @RequestParam Integer status,
                                HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        userService.updateStatus(userId, status);
        return R.ok();
    }

    @ApiOperation("重置用户密码为默认123456")
    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        User user = new User();
        user.setId(id);
        user.setPassword("123456");
        userService.updateUser(user);
        return R.ok();
    }

    @ApiOperation("用户统计（ADMIN）")
    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        return R.ok(userService.statistics());
    }
}
