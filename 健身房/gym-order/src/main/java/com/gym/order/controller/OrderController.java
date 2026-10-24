package com.gym.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.api.user.UserFeignClient;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.order.OrderItem;
import com.gym.entity.order.Orders;
import com.gym.entity.user.User;
import com.gym.order.dto.OrderCreateDTO;
import com.gym.order.dto.OrderVO;
import com.gym.order.service.OrderItemService;
import com.gym.order.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrdersService ordersService;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private UserFeignClient userFeignClient;

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

    @ApiOperation("管理员订单全量分页（聚合用户与商品摘要）")
    @GetMapping("/page")
    public R<PageResult<OrderVO>> page(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("订单状态（可选）") @RequestParam(required = false) Integer status,
            @ApiParam("订单号模糊（可选）") @RequestParam(required = false) String orderNo,
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
                wrapper.eq(Orders::getStatus, resolveStatus(status));
            }
            if (orderNo != null && !orderNo.trim().isEmpty()) {
                wrapper.like(Orders::getOrderNo, orderNo.trim());
            }
            wrapper.orderByDesc(Orders::getCreateTime);
            result = ordersService.page(page, wrapper);
        }
        List<OrderVO> voList = toVOList(result.getRecords());
        return R.ok(PageResult.of(voList, result.getTotal()));
    }

    @ApiOperation("管理员变更订单状态")
    @PutMapping("/{orderNo}/status")
    public R<Void> updateStatus(@ApiParam("订单号") @PathVariable String orderNo,
                                @ApiParam("状态：0待付款1已付款2已发货3已完成4已取消5已退款") @RequestParam Integer status,
                                HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        if (ordersService.getByOrderNo(orderNo) == null) {
            return R.fail(ResultCode.ORDER_NOT_EXIST);
        }
        ordersService.updateOrderStatus(orderNo, status);
        return R.ok();
    }

    private List<OrderVO> toVOList(List<Orders> orders) {
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量查用户（避免 N+1）
        List<Long> userIds = orders.stream()
                .map(Orders::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                R<List<User>> userR = userFeignClient.getUserByIds(userIds);
                if (userR != null && userR.isSuccess() && userR.getData() != null) {
                    for (User u : userR.getData()) {
                        if (u != null && u.getId() != null) {
                            userMap.put(u.getId(), u);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("批量聚合订单用户信息失败 userIds={}", userIds, e);
            }
        }
        // 批量查订单明细，取每个订单首条
        List<String> orderNos = orders.stream().map(Orders::getOrderNo).collect(Collectors.toList());
        Map<String, OrderItem> firstItemMap = new HashMap<>();
        List<OrderItem> items = orderItemService.listByOrderNos(orderNos);
        for (OrderItem it : items) {
            firstItemMap.putIfAbsent(it.getOrderId(), it);
        }
        // 组装
        List<OrderVO> voList = new ArrayList<>(orders.size());
        for (Orders order : orders) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            User u = userMap.get(order.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setPhone(u.getPhone());
            }
            OrderItem first = firstItemMap.get(order.getOrderNo());
            if (first != null) {
                vo.setGoodsName(first.getGoodsName());
                vo.setGoodsCover(first.getGoodsCover());
                vo.setQuantity(first.getQuantity());
                vo.setPrice(first.getPrice());
            }
            voList.add(vo);
        }
        return voList;
    }

    private OrderStatus resolveStatus(Integer code) {
        if (code == null) return null;
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getCode().equals(code)) return s;
        }
        return null;
    }
}
