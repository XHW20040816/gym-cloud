package com.gym.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.seckill.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    @Select("SELECT COUNT(1) FROM t_seckill_order WHERE activity_id = #{activityId} AND user_id = #{userId}")
    int countByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
