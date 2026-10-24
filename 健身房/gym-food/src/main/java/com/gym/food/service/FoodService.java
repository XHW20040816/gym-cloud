package com.gym.food.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.food.Food;

import java.util.List;

public interface FoodService extends IService<Food> {

    IPage<Food> listPage(Page<Food> page, String nameLike, Integer status);

    List<Food> listAll(Integer status);
}
