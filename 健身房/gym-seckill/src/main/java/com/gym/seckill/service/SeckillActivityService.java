package com.gym.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.seckill.SeckillActivity;

import java.util.List;

public interface SeckillActivityService extends IService<SeckillActivity> {

    IPage<SeckillActivity> listPage(Page<SeckillActivity> page, Integer status, String name);

    /** 查询进行中的秒杀活动列表（public 用） */
    List<SeckillActivity> listOngoing();

    /** 删除活动并级联删除其下秒杀商品 */
    void deleteWithGoods(Long id);
}
