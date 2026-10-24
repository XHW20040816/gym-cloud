package com.gym.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.order.OrderItem;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {

    /** 根据订单号查询明细列表 */
    List<OrderItem> listByOrderNo(String orderNo);

    /** 批量根据订单号查询明细列表 */
    List<OrderItem> listByOrderNos(List<String> orderNos);
}
