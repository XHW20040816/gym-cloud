package com.gym.order.controller.internal;

import com.gym.common.result.R;
import com.gym.entity.order.Orders;
import com.gym.order.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/order/internal")
public class InternalOrderController {

    @Resource
    private OrdersService ordersService;

    @PostMapping("/status")
    public R<Void> updateOrderStatus(@RequestParam String orderNo,
                                     @RequestParam Integer status) {
        boolean ok = ordersService.updateOrderStatus(orderNo, status);
        return ok ? R.ok() : R.fail(500, "订单状态更新失败");
    }

    @GetMapping("/{orderNo}")
    public R<Orders> getByOrderNo(@PathVariable String orderNo) {
        Orders order = ordersService.getByOrderNo(orderNo);
        return R.ok(order);
    }
}
