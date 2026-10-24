package com.gym.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.food.Food;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}
