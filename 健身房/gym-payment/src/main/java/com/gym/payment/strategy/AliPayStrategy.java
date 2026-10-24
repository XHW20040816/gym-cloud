package com.gym.payment.strategy;

import com.gym.api.dto.PayDTO;
import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.PayType;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付策略。
 *
 * <p>真实生产环境需接入支付宝开放平台（alipay.trade.page.pay）并实现异步回调验签；
 * 当前未配置商户信息，暂不支持，调用时明确报错，避免使用模拟支付单。
 */
@Component
public class AliPayStrategy implements PayStrategy {

    @Override
    public PayType getType() {
        return PayType.ALIPAY;
    }

    @Override
    public String pay(PayDTO dto) {
        throw new BizException(ResultCode.PAY_ERROR, "支付宝支付暂未接入，请使用余额支付");
    }
}
