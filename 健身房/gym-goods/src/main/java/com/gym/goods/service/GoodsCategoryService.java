package com.gym.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.goods.GoodsCategory;

public interface GoodsCategoryService extends IService<GoodsCategory> {

    IPage<GoodsCategory> listPage(Page<GoodsCategory> page, String nameLike);
}
