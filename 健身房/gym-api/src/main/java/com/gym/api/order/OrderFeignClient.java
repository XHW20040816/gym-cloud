package com.gym.api.order;

import com.gym.common.result.R;
import com.gym.entity.order.Orders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gym-order")
public interface OrderFeignClient {

    @PostMapping("/order/internal/status")
    R<Void> updateOrderStatus(@RequestParam("orderNo") String orderNo,
                              @RequestParam("status") Integer status);

    @GetMapping("/order/internal/{orderNo}")
    R<Orders> getByOrderNo(@PathVariable("orderNo") String orderNo);
}
