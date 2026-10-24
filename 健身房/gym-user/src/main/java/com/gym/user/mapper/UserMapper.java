package com.gym.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 扣除用户余额（原子SQL：余额足够才扣）
     * @return 影响行数 0=余额不足
     */
    @Update("UPDATE t_user SET balance = balance - #{amount} WHERE id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
