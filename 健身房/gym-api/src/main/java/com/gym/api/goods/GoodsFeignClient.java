package com.gym.api.goods;

import com.gym.common.result.R;
import com.gym.entity.goods.Goods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gym-goods")
public interface GoodsFeignClient {

    @GetMapping("/goods/internal/{id}")
    R<Goods> getById(@PathVariable("id") Long id);

    @PostMapping("/goods/internal/deductStock")
    R<Void> deductStock(@RequestParam("id") Long id,
                        @RequestParam("count") Integer count);

    @PostMapping("/goods/internal/addStock")
    R<Void> addStock(@RequestParam("id") Long id,
                     @RequestParam("count") Integer count);
}
