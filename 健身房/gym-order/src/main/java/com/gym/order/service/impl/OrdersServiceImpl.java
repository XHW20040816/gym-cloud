package com.gym.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.api.food.FoodFeignClient;
import com.gym.api.goods.GoodsFeignClient;
import com.gym.common.constant.MQConstants;
import com.gym.common.exception.BizException;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.common.util.SnowflakeIdUtil;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.food.Food;
import com.gym.entity.goods.Goods;
import com.gym.entity.order.OrderItem;
import com.gym.entity.order.Orders;
import com.gym.order.dto.OrderCreateDTO;
import com.gym.order.dto.OrderItemDTO;
import com.gym.order.mapper.OrdersMapper;
import com.gym.order.service.OrderItemService;
import com.gym.order.service.OrdersService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private GoodsFeignClient goodsFeignClient;

    @Resource
    private FoodFeignClient foodFeignClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SnowflakeIdUtil snowflakeIdUtil;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, name = "create-order-tx")
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(OrderCreateDTO dto) {
        // ① 生成订单号
        String orderNo = snowflakeIdUtil.nextIdStr();

        // ② 处理明细：查商品 → 计算总价 → 填充商品信息
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemDTO> items = dto.getItems();
        if (items == null || items.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "订单明细不能为空");
        }

        for (OrderItemDTO itemDTO : items) {
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new BizException(ResultCode.PARAM_ERROR, "商品数量必须大于0");
            }
            int itemType = itemDTO.getItemType() != null ? itemDTO.getItemType() : OrderItem.ITEM_TYPE_GOODS;

            String name;
            String cover;
            BigDecimal price;
            if (itemType == OrderItem.ITEM_TYPE_FOOD) {
                // 餐饮：价格以 gym-food 为准；餐饮无库存，不扣减商品库存
                R<Food> foodR = foodFeignClient.getById(itemDTO.getGoodsId());
                if (foodR == null || !foodR.isSuccess() || foodR.getData() == null) {
                    throw new BizException(ResultCode.PARAM_ERROR, "餐品不存在: " + itemDTO.getGoodsId());
                }
                Food food = foodR.getData();
                name = food.getName();
                cover = food.getImage();
                price = food.getPrice();
            } else {
                // 商品：远程获取商品信息，并逐条扣减库存（失败抛异常 → 事务回滚）
                R<Goods> goodsR = goodsFeignClient.getById(itemDTO.getGoodsId());
                if (goodsR == null || !goodsR.isSuccess() || goodsR.getData() == null) {
                    throw new BizException(ResultCode.PARAM_ERROR, "商品不存在: " + itemDTO.getGoodsId());
                }
                Goods goods = goodsR.getData();
                name = goods.getName();
                cover = goods.getCover();
                price = goods.getPrice();

                R<Void> deductR = goodsFeignClient.deductStock(itemDTO.getGoodsId(), itemDTO.getQuantity());
                if (deductR == null || !deductR.isSuccess()) {
                    throw new BizException(ResultCode.STOCK_NOT_ENOUGH, "商品库存扣减失败: " + goods.getName());
                }
            }

            // ③ 计算小计和总价
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 构造 OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderNo);
            orderItem.setGoodsId(itemDTO.getGoodsId());
            orderItem.setGoodsName(name);
            orderItem.setGoodsCover(cover);
            orderItem.setPrice(price);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setItemType(itemType);
            orderItems.add(orderItem);
        }

        // ⑤ 插入订单主表
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(OrderStatus.UNPAID);
        order.setDeliveryInfo(dto.getDeliveryInfo());
        order.setRemark(dto.getRemark());
        ordersMapper.insert(order);

        // 批量插入订单明细
        if (!orderItems.isEmpty()) {
            orderItemService.saveBatch(orderItems);
        }

        // ⑥ 发送订单 TTL 延迟消息
        // 消息先进入 ORDER_TTL_QUEUE，30分钟后未消费则根据 x-dead-letter-exchange 路由到 ORDER_DLX_QUEUE
        // 死信消费者 OrderTimeoutConsumer 监听到后执行超时取消
        // TTL 由队列级 x-message-ttl 统一决定（单一来源），消息上不再单独设置过期时间
        rabbitTemplate.convertAndSend(
                MQConstants.ORDER_TTL_EXCHANGE,
                MQConstants.ORDER_TTL_QUEUE, // 与 framework 的 binding routing key 保持一致
                orderNo
        );
        log.info("订单创建成功，已发送TTL延迟消息，30分钟后如未支付将自动取消 orderNo={}", orderNo);

        return order;
    }

    @Override
    public Orders getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        return getOne(wrapper);
    }

    @Override
    public IPage<Orders> listPageByUserId(Long userId, Integer status, Page<Orders> page) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean updateOrderStatus(String orderNo, Integer status) {
        int rows = ordersMapper.updateOrderStatus(orderNo, status);
        return rows > 0;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, name = "cancel-order-tx")
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        // ① 查出订单，判断是否为 UNPAID
        Orders order = getByOrderNo(orderNo);
        if (order == null) {
            log.warn("取消订单失败，订单不存在 orderNo={}", orderNo);
            return;
        }
        if (order.getStatus() != OrderStatus.UNPAID) {
            log.warn("取消订单失败，订单状态不是未支付 orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        // ② 更新订单状态为已取消
        boolean updated = updateOrderStatus(orderNo, OrderStatus.CANCELLED.getCode());
        if (!updated) {
            throw new BizException("订单状态更新失败");
        }
        log.info("订单状态已更新为已取消 orderNo={}", orderNo);

        // ③ 回滚库存：根据 orderNo 查询明细 → 逐条 addStock
        // 任一条回滚失败即抛异常 → @Transactional 回滚，订单状态保持 UNPAID，等待重试或人工处理
        List<OrderItem> orderItemList = orderItemService.listByOrderNo(orderNo);
        for (OrderItem item : orderItemList) {
            // 餐饮明细没有扣减过商品库存，取消时也不回滚
            if (item.getItemType() != null && item.getItemType() == OrderItem.ITEM_TYPE_FOOD) {
                continue;
            }
            R<Void> addR = goodsFeignClient.addStock(item.getGoodsId(), item.getQuantity());
            if (addR == null || !addR.isSuccess()) {
                throw new BizException(ResultCode.FAIL,
                        "回滚库存失败 goodsId=" + item.getGoodsId() + ", qty=" + item.getQuantity());
            }
        }

        log.info("订单取消成功，库存回滚完成 orderNo={}", orderNo);
    }
}
