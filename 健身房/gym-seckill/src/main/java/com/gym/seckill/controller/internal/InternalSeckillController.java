package com.gym.seckill.controller.internal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.result.R;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.seckill.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 秒杀服务内部接口（供 Feign 调用，不在 Swagger 文档显示）。
 */
@RestController
@RequestMapping("/seckill/internal")
public class InternalSeckillController {

    @Resource
    private SeckillGoodsService seckillGoodsService;

    @DeleteMapping("/goods/{goodsId}")
    public R<Void> deleteGoodsByGoodsId(@PathVariable Long goodsId) {
        seckillGoodsService.remove(
                new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getGoodsId, goodsId));
        return R.ok();
    }
}
