package com.gym.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.order.Orders;
import com.gym.order.dto.OrderCreateDTO;

public interface OrdersService extends IService<Orders> {

    /**
     * 创建普通商品订单
     * ① SnowflakeIdUtil 生成 orderNo
     * ② 循环 items 通过 goodsFeignClient.getById 查商品 → 计算总价 → 设置 goodsName/goodsCover/price/subtotal
     * ③ 逐条 Feign deductStock 扣减库存（失败抛异常回滚事务）
     * ④ 插入 orders + 批量 insert orderItems
     * ⑤ 发送订单 TTL 延迟消息（30分钟后进死信 ORDER_DLX_QUEUE → 自动取消）
     */
    Orders createOrder(OrderCreateDTO dto);

    /** 根据订单号查询 */
    Orders getByOrderNo(String orderNo);

    /** 按用户ID分页查询，可按status过滤 */
    IPage<Orders> listPageByUserId(Long userId, Integer status, Page<Orders> page);

    /** 更新订单状态 */
    boolean updateOrderStatus(String orderNo, Integer status);

    /**
     * 取消订单（超时取消/用户主动取消）
     * ① 判 UNPAID，否则直接返回
     * ② updateOrderStatus → CANCELLED(4)
     * ③ 循环 orderItems → goodsFeignClient.addStock 回滚库存
     */
    void cancelOrder(String orderNo);
}
