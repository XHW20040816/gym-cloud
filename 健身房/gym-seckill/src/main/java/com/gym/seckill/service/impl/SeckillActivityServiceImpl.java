package com.gym.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.seckill.SeckillActivity;
import com.gym.seckill.mapper.SeckillActivityMapper;
import com.gym.seckill.service.SeckillActivityService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeckillActivityServiceImpl
        extends ServiceImpl<SeckillActivityMapper, SeckillActivity>
        implements SeckillActivityService {

    @Override
    public IPage<SeckillActivity> listPage(Page<SeckillActivity> page, Integer status) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SeckillActivity::getStatus, status);
        }
        wrapper.orderByDesc(SeckillActivity::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public List<SeckillActivity> listOngoing() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivity::getStatus, SeckillActivity.STATUS_ONGOING);
        wrapper.le(SeckillActivity::getStartTime, now);
        wrapper.ge(SeckillActivity::getEndTime, now);
        wrapper.orderByAsc(SeckillActivity::getStartTime);
        return list(wrapper);
    }
}
