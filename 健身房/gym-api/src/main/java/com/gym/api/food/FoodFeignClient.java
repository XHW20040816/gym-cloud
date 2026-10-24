package com.gym.api.food;

import com.gym.common.result.R;
import com.gym.entity.food.Food;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gym-food")
public interface FoodFeignClient {

    @GetMapping("/food/{id}")
    R<Food> getById(@PathVariable("id") Long id);
}
