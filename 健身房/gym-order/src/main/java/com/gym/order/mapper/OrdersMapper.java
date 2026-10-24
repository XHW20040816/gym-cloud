package com.gym.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.order.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    @Update("UPDATE t_orders SET status = #{status} WHERE order_no = #{orderNo}")
    int updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
}
