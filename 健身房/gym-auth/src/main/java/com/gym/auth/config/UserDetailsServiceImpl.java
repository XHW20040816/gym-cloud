package com.gym.auth.config;

import com.gym.api.user.UserFeignClient;
import com.gym.common.result.R;
import com.gym.entity.enums.RoleType;
import com.gym.entity.user.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 UserDetailsService：通过 Feign 调用 gym-user 服务获取用户
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 通过 Feign 远程调用用户服务
        R<User> result = userFeignClient.getUserByUsername(username);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        User user = result.getData();
        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        RoleType role = user.getRole();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.name()));
        }

        // 返回 Spring Security 自带的 User 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword() != null ? user.getPassword() : "",
                authorities
        );
    }
}
