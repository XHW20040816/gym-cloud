package com.gym.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService extends IService<SeckillGoods> {

    List<SeckillGoods> listByActivityId(Long activityId);
}
