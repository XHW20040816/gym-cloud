package com.gym.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.payment.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM t_payment WHERE order_no = #{orderNo} AND pay_type = #{payType}")
    Payment selectByOrderAndType(@Param("orderNo") String orderNo, @Param("payType") Integer payType);
}
