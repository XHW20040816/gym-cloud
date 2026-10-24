package com.gym.payment.strategy;

import com.gym.api.dto.PayDTO;
import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.PayType;
import org.springframework.stereotype.Component;

/**
 * 微信支付策略。
 *
 * <p>真实生产环境需接入微信支付（JSAPI/Native 下单）并实现回调验签；
 * 当前未配置商户信息，暂不支持，调用时明确报错，避免使用模拟支付单。
 */
@Component
public class WxPayStrategy implements PayStrategy {

    @Override
    public PayType getType() {
        return PayType.WECHAT;
    }

    @Override
    public String pay(PayDTO dto) {
        throw new BizException(ResultCode.PAY_ERROR, "微信支付暂未接入，请使用余额支付");
    }
}
