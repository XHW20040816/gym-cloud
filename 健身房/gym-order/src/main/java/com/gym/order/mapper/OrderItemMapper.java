package com.gym.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.order.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
