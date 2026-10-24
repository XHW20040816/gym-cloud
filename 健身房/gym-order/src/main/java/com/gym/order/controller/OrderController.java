package com.gym.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.order.OrderItem;
import com.gym.entity.order.Orders;
import com.gym.order.dto.OrderCreateDTO;
import com.gym.order.service.OrderItemService;
import com.gym.order.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrdersService ordersService;

    @Resource
    private OrderItemService orderItemService;

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public R<String> create(@RequestBody OrderCreateDTO dto, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        dto.setUserId(Long.valueOf(userIdStr));
        Orders order = ordersService.createOrder(dto);
        return R.ok(order.getOrderNo());
    }

    @ApiOperation("我的订单列表（分页）")
    @GetMapping("/my")
    public R<PageResult<Orders>> my(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("订单状态（可选）") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        Page<Orders> page = new Page<>(pageNum, pageSize);
        IPage<Orders> result = ordersService.listPageByUserId(userId, status, page);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @ApiOperation("订单详情（含明细）")
    @GetMapping("/{orderNo}")
    public R<Map<String, Object>> detail(@PathVariable String orderNo, HttpServletRequest request) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.fail(ResultCode.ORDER_NOT_EXIST);
        }
        // 鉴权：本人或 ADMIN
        String userIdStr = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            if (userIdStr == null || !userIdStr.equals(String.valueOf(order.getUserId()))) {
                return R.fail(ResultCode.FORBIDDEN);
            }
        }
        List<OrderItem> items = orderItemService.listByOrderNo(orderNo);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return R.ok(data);
    }

    @ApiOperation("用户主动取消订单")
    @PostMapping("/cancel/{orderNo}")
    public R<Void> cancel(@PathVariable String orderNo, HttpServletRequest request) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.fail(ResultCode.ORDER_NOT_EXIST);
        }
        // 鉴权：本人或 ADMIN
        String userIdStr = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            if (userIdStr == null || !userIdStr.equals(String.valueOf(order.getUserId()))) {
                return R.fail(ResultCode.FORBIDDEN);
            }
        }
        ordersService.cancelOrder(orderNo);
        return R.ok();
    }

    @ApiOperation("管理员订单全量分页")
    @GetMapping("/page")
    public R<PageResult<Orders>> page(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("订单状态（可选）") @RequestParam(required = false) Integer status,
            @ApiParam("用户ID（可选）") @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        Page<Orders> page = new Page<>(pageNum, pageSize);
        IPage<Orders> result;
        if (userId != null) {
            result = ordersService.listPageByUserId(userId, status, page);
        } else {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(Orders::getStatus, status);
            }
            wrapper.orderByDesc(Orders::getCreateTime);
            result = ordersService.page(page, wrapper);
        }
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }
}
