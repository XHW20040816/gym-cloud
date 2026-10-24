package com.gym.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.goods.GoodsCategory;
import com.gym.goods.mapper.GoodsCategoryMapper;
import com.gym.goods.service.GoodsCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    @Resource
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public IPage<GoodsCategory> listPage(Page<GoodsCategory> page, String nameLike) {
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(nameLike)) {
            wrapper.like(GoodsCategory::getName, nameLike);
        }
        wrapper.orderByAsc(GoodsCategory::getSort);
        return goodsCategoryMapper.selectPage(page, wrapper);
    }
}
