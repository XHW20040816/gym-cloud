package com.gym.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.goods.Goods;

public interface GoodsService extends IService<Goods> {

    IPage<Goods> listPage(Page<Goods> page, Long categoryId, String nameLike, Integer status);

    Goods getById(Long id);

    boolean saveOrUpdateGoods(Goods goods);

    boolean removeGoods(Long id);

    boolean deductStock(Long id, Integer count);

    boolean addStock(Long id, Integer count);

    boolean updateStatus(Long id, Integer status);
}
