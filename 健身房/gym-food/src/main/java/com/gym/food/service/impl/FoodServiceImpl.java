package com.gym.food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.food.Food;
import com.gym.food.mapper.FoodMapper;
import com.gym.food.service.FoodService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FoodServiceImpl extends ServiceImpl<FoodMapper, Food> implements FoodService {

    @Resource
    private FoodMapper foodMapper;

    @Override
    public IPage<Food> listPage(Page<Food> page, String nameLike, Integer status) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(nameLike)) {
            wrapper.like(Food::getName, nameLike);
        }
        if (status != null) {
            wrapper.eq(Food::getStatus, status);
        }
        wrapper.orderByDesc(Food::getCreateTime);
        return foodMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Food> listAll(Integer status) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Food::getStatus, status);
        }
        wrapper.orderByDesc(Food::getCreateTime);
        return foodMapper.selectList(wrapper);
    }
}
