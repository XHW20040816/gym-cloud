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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBalance(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("退款金额必须大于0");
        }
        int rows = userMapper.addBalance(userId, amount);
        if (rows == 0) {
            throw new BizException("用户不存在");
        }
        return true;
    }

    @Override
    public Map<String, Object> statistics() {
        long total = userMapper.countAll();

        Map<String, Long> roleCounts = new LinkedHashMap<>();
        roleCounts.put("ADMIN", 0L);
        roleCounts.put("COACH", 0L);
        roleCounts.put("USER", 0L);
        for (Map<String, Object> row : userMapper.countByRole()) {
            Object r = row.get("role");
            Object cnt = row.get("cnt");
            if (r != null) {
                roleCounts.put(String.valueOf(r), cnt == null ? 0L : Long.parseLong(String.valueOf(cnt)));
            }
        }

        LocalDateTime start = LocalDateTime.now().minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Map<String, Object>> rows = userMapper.countGroupByDay(
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Map<String, Long> dayMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object d = row.get("d");
            Object cnt = row.get("cnt");
            if (d != null) {
                dayMap.put(String.valueOf(d), cnt == null ? 0L : Long.parseLong(String.valueOf(cnt)));
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDateTime day = start.plusDays(i);
            String key = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.format(DateTimeFormatter.ofPattern("MM-dd")));
            point.put("count", dayMap.getOrDefault(key, 0L));
            trend.add(point);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("trend", trend);
        data.put("roleCounts", roleCounts);
        return data;
    }
}
