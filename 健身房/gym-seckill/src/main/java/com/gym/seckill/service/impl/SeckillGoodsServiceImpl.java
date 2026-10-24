package com.gym.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.seckill.mapper.SeckillGoodsMapper;
import com.gym.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl
        extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
        implements SeckillGoodsService {

    @Override
    public List<SeckillGoods> listByActivityId(Long activityId) {
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillGoods::getActivityId, activityId);
        wrapper.orderByAsc(SeckillGoods::getSort);
        return list(wrapper);
    }
}
