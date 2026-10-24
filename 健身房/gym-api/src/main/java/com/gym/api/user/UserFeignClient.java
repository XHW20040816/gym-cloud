package com.gym.api.user;

import com.gym.common.result.R;
import com.gym.entity.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("gym-user")
public interface UserFeignClient {

    @GetMapping("/user/internal/{id}")
    R<User> getUserById(@PathVariable("id") Long id);

    @PostMapping("/user/internal/listByIds")
    R<List<User>> getUserByIds(@RequestBody List<Long> ids);

    @GetMapping("/user/internal/username/{username}")
    R<User> getUserByUsername(@PathVariable("username") String username);

    @PostMapping("/user/internal/deductBalance")
    R<Void> deductBalance(@RequestParam("userId") Long userId,
                          @RequestParam("amount") BigDecimal amount);

    @PostMapping("/user/internal/addBalance")
    R<Void> addBalance(@RequestParam("userId") Long userId,
                       @RequestParam("amount") BigDecimal amount);

    /** 注册新用户（默认 USER 角色），返回用户ID */
    @PostMapping("/user/internal/register")
    R<Long> register(@RequestBody User user);
}
