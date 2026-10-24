package com.gym.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.common.exception.BizException;
import com.gym.entity.enums.RoleType;
import com.gym.entity.enums.UserStatus;
import com.gym.entity.user.User;
import com.gym.user.dto.UserDTO;
import com.gym.user.mapper.UserMapper;
import com.gym.user.service.UserService;
import cn.hutool.crypto.digest.BCrypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return super.getById(id);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public IPage<User> listPage(Page<User> page, UserDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.isNotBlank(dto.getUsername())) {
                wrapper.like(User::getUsername, dto.getUsername());
            }
            if (StringUtils.isNotBlank(dto.getNickname())) {
                wrapper.like(User::getNickname, dto.getNickname());
            }
            if (StringUtils.isNotBlank(dto.getPhone())) {
                wrapper.like(User::getPhone, dto.getPhone());
            }
            if (StringUtils.isNotBlank(dto.getRole())) {
                wrapper.eq(User::getRole, RoleType.valueOf(dto.getRole()));
            }
            if (dto.getStatus() != null) {
                wrapper.eq(User::getStatus, dto.getStatus() == 1 ? UserStatus.NORMAL : UserStatus.DISABLED);
            }
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(User user) {
        // 密码加密（Hutool BCrypt，与 Spring Security BCryptPasswordEncoder 格式兼容）
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        // 默认余额0，积分0
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        if (user.getPoint() == null) {
            user.setPoint(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.NORMAL);
        }
        if (user.getRole() == null) {
            user.setRole(RoleType.USER);
        }
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        // 如果密码不为空则加密更新
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status == 1 ? UserStatus.NORMAL : UserStatus.DISABLED);
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("扣减金额必须大于0");
        }
        int rows = userMapper.deductBalance(userId, amount);
        if (rows == 0) {
            throw new BizException("用户余额不足");
        }
        return true;
    }
}
