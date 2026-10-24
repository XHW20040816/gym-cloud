package com.gym.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 扣除用户余额（原子SQL：余额足够才扣）
     * @return 影响行数 0=余额不足
     */
    @Update("UPDATE t_user SET balance = balance - #{amount} WHERE id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 增加用户余额（退款等场景，原子 SQL）
     * @return 影响行数 0=用户不存在
     */
    @Update("UPDATE t_user SET balance = balance + #{amount} WHERE id = #{userId}")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Select("SELECT COUNT(*) FROM t_user")
    long countAll();

    @Select("SELECT role, COUNT(*) AS cnt FROM t_user GROUP BY role")
    List<Map<String, Object>> countByRole();

    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS cnt FROM t_user WHERE create_time >= #{start} GROUP BY DATE(create_time) ORDER BY d")
    List<Map<String, Object>> countGroupByDay(@Param("start") String start);
}
