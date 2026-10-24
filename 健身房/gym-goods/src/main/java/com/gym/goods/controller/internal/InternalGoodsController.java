package com.gym.goods.controller.internal;

import com.gym.common.result.R;
import com.gym.entity.goods.Goods;
import com.gym.goods.service.GoodsService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "内部接口", hidden = true)
@RestController
@RequestMapping("/goods/internal")
public class InternalGoodsController {

    @Resource
    private GoodsService goodsService;

    @GetMapping("/{id}")
    public R<Goods> getById(@PathVariable("id") Long id) {
        Goods goods = goodsService.getById(id);
        return R.ok(goods);
    }

    @PostMapping("/deductStock")
    public R<Void> deductStock(@RequestParam("id") Long id,
                               @RequestParam("count") Integer count) {
        goodsService.deductStock(id, count);
        return R.ok();
    }

    @PostMapping("/addStock")
    public R<Void> addStock(@RequestParam("id") Long id,
                            @RequestParam("count") Integer count) {
        goodsService.addStock(id, count);
        return R.ok();
    }
}
