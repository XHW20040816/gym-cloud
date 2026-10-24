package com.gym.payment.strategy;

import com.gym.api.dto.PayDTO;
import com.gym.api.order.OrderFeignClient;
import com.gym.api.user.UserFeignClient;
import com.gym.common.constant.RedisConstants;
import com.gym.common.exception.BizException;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.enums.PayType;
import com.gym.entity.order.Orders;
import com.gym.entity.payment.Payment;
import com.gym.payment.mapper.PaymentMapper;
import com.gym.payment.service.BalancePayTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalancePayStrategy implements PayStrategy {

    private final RedissonClient redissonClient;
    private final PaymentMapper paymentMapper;
    private final BalancePayTxService balancePayTxService;

    @Autowired
    @Lazy
    private OrderFeignClient orderFeignClient;

    @Autowired
    @Lazy
    private UserFeignClient userFeignClient;

    @Override
    public PayType getType() {
        return PayType.BALANCE;
    }

    @Override
    public String pay(PayDTO dto) {
        // 1) 幂等：先查是否已支付
        Payment exist = paymentMapper.selectByOrderAndType(dto.getOrderNo(), PayType.BALANCE.getCode());
        if (exist != null && Payment.STATUS_SUCCESS == exist.getStatus()) {
            return exist.getPaymentNo();
        }

        // 2) 分布式锁防重复支付：pay:lock:{orderNo}:{payType}，30 秒
        RLock lock = redissonClient.getLock(RedisConstants.PAY_LOCK_KEY + dto.getOrderNo() + ":balance");
        try {
            if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                throw new BizException(ResultCode.PAY_ERROR.getCode(), "请勿重复支付");
            }

            // 3) 幂等再查一次（双重检查锁）
            exist = paymentMapper.selectByOrderAndType(dto.getOrderNo(), PayType.BALANCE.getCode());
            if (exist != null && Payment.STATUS_SUCCESS == exist.getStatus()) {
                return exist.getPaymentNo();
            }

            // 4) 校验订单：必须存在、未支付、金额与用户匹配
            R<Orders> orderR = orderFeignClient.getByOrderNo(dto.getOrderNo());
            if (orderR == null || orderR.getCode() != 200 || orderR.getData() == null) {
                throw new BizException(ResultCode.ORDER_NOT_EXIST);
            }
            Orders order = orderR.getData();
            if (order.getStatus() != OrderStatus.UNPAID) {
                throw new BizException(ResultCode.PAY_ERROR, "订单状态错误");
            }
            // 用 compareTo 比较数值，避免 BigDecimal 精度（59.8 vs 59.80）导致误判
            if (dto.getAmount() == null || order.getPayAmount().compareTo(dto.getAmount()) != 0) {
                throw new BizException(ResultCode.PAY_ERROR, "支付金额不符");
            }
            if (!order.getUserId().equals(dto.getUserId())) {
                throw new BizException(ResultCode.PAY_ERROR, "订单所属用户不符");
            }

            // 5) 独立 Bean 执行 @GlobalTransactional，跨 user/payment/order 三库一致
            return balancePayTxService.doBalancePay(dto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.PAY_ERROR, "支付被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
