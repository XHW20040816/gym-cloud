package com.gym.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.seckill.SeckillActivity;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.seckill.mapper.SeckillActivityMapper;
import com.gym.seckill.service.SeckillActivityService;
import com.gym.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeckillActivityServiceImpl
        extends ServiceImpl<SeckillActivityMapper, SeckillActivity>
        implements SeckillActivityService {

    @Resource
    private SeckillGoodsService seckillGoodsService;

    @Override
    public IPage<SeckillActivity> listPage(Page<SeckillActivity> page, Integer status, String name) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SeckillActivity::getStatus, status);
        }
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(SeckillActivity::getName, name.trim());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithGoods(Long id) {
        // ① 先级联删除该活动下的秒杀商品，避免孤儿数据
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillGoods::getActivityId, id);
        seckillGoodsService.remove(wrapper);
        // ② 再删除活动
        removeById(id);
    }
}
