package com.gym.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.payment.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM t_payment WHERE order_no = #{orderNo} AND pay_type = #{payType}")
    Payment selectByOrderAndType(@Param("orderNo") String orderNo, @Param("payType") Integer payType);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_payment WHERE status = 1")
    BigDecimal sumSuccessAmount();

    @Select("SELECT COUNT(*) FROM t_payment WHERE status = 1")
    long countSuccess();

    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_payment WHERE status = 3")
    BigDecimal sumRefundAmount();

    @Select("SELECT COUNT(*) FROM t_payment WHERE status = 2")
    long countFail();

    @Select("SELECT DATE(pay_time) AS d, SUM(amount) AS amt FROM t_payment WHERE status = 1 AND pay_time >= #{start} GROUP BY DATE(pay_time) ORDER BY d")
    List<Map<String, Object>> sumAmountGroupByDay(@Param("start") String start);
}
