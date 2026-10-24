package com.gym.payment.strategy;

import com.gym.api.dto.PayDTO;
import com.gym.entity.enums.PayType;

public interface PayStrategy {

    /**
     * 获取支付方式类型
     * @return 支付类型枚举
     */
    PayType getType();

    /**
     * 支付执行
     * @param payDTO 支付请求 dto(userId, orderNo, payType, amount)
     * @return 支付成功后返回 paymentNo
     */
    String pay(PayDTO payDTO);
}
