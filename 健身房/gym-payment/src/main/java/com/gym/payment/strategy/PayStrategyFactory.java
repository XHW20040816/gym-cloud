package com.gym.payment.strategy;

import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.PayType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PayStrategyFactory {

    private final Map<PayType, PayStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    public void register(List<PayStrategy> list) {
        list.forEach(s -> strategyMap.put(s.getType(), s));
    }

    public PayStrategy get(PayType type) {
        PayStrategy s = strategyMap.get(type);
        if (s == null) {
            throw new BizException(ResultCode.PAY_ERROR, "不支持的支付方式");
        }
        return s;
    }
}
