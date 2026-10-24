package com.gym.api.user;

import com.gym.common.result.R;
import com.gym.entity.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient("gym-user")
public interface UserFeignClient {

    @GetMapping("/user/internal/{id}")
    R<User> getUserById(@PathVariable("id") Long id);

    @GetMapping("/user/internal/username/{username}")
    R<User> getUserByUsername(@PathVariable("username") String username);

    @PostMapping("/user/internal/deductBalance")
    R<Void> deductBalance(@RequestParam("userId") Long userId,
                          @RequestParam("amount") BigDecimal amount);
}
