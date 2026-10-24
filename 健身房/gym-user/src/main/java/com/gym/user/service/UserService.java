package com.gym.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.user.User;
import com.gym.user.dto.UserDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface UserService extends IService<User> {

    User getById(Long id);

    User getByUsername(String username);

    IPage<User> listPage(Page<User> page, UserDTO dto);

    boolean saveUser(User user);

    boolean updateUser(User user);

    boolean removeById(Long id);

    boolean updateStatus(Long userId, Integer status);

    /**
     * 扣减用户余额
     * @param userId 用户ID
     * @param amount 扣减金额
     * @return 成功返回true，余额不足抛出BizException
     */
    boolean deductBalance(Long userId, BigDecimal amount);

    /**
     * 增加用户余额（退款场景）
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 成功返回true
     */
    boolean addBalance(Long userId, BigDecimal amount);

    /**
     * 用户统计：总数、近30天增长趋势、角色分布
     */
    Map<String, Object> statistics();
}
