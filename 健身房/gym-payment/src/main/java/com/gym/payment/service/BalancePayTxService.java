package com.gym.payment.service;

import com.gym.api.dto.PayDTO;
import com.gym.api.order.OrderFeignClient;
import com.gym.api.user.UserFeignClient;
import com.gym.common.exception.BizException;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.common.util.SnowflakeIdUtil;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.enums.PayType;
import com.gym.entity.payment.Payment;
import com.gym.payment.mapper.PaymentMapper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 余额支付全局事务执行器。
 *
 * <p>独立成 Bean 是为了让 {@link GlobalTransactional} 通过 Spring AOP 代理生效：
 * 若放在 BalancePayStrategy 内部由 this 自调用，代理会被绕过，全局事务不会开启。</p>
 *
 * <p>跨库分支：gym-user 扣余额 → gym-payment 写流水 → gym-order 改订单状态，
 * 任一步失败由 Seata AT 回滚已提交的分支。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalancePayTxService {

    private final PaymentMapper paymentMapper;
    private final SnowflakeIdUtil idUtil;
    private final OrderFeignClient orderFeignClient;
    private final UserFeignClient userFeignClient;

    @GlobalTransactional(rollbackFor = Exception.class, name = "balance-pay-tx")
    public String doBalancePay(PayDTO dto) {
        // ① 扣余额：余额不足时 user 服务抛异常，全局事务回滚
        R<Void> r1 = userFeignClient.deductBalance(dto.getUserId(), dto.getAmount());
        if (r1 == null || r1.getCode() != 200) {
            String msg = (r1 != null && r1.getMsg() != null) ? r1.getMsg() : "余额扣减失败";
            throw new BizException(ResultCode.PAY_ERROR, msg);
        }

        // ② 插入支付流水：UNIQUE(orderNo, payType) 冲突抛异常，全局回滚（含已扣余额）
        Payment pay = new Payment();
        pay.setPaymentNo(idUtil.nextIdStr());
        pay.setOrderNo(dto.getOrderNo());
        pay.setUserId(dto.getUserId());
        pay.setAmount(dto.getAmount());
        pay.setPayType(PayType.BALANCE);
        pay.setStatus(Payment.STATUS_SUCCESS);
        pay.setPayTime(LocalDateTime.now());
        pay.setCreateTime(LocalDateTime.now());
        pay.setTransactionId("BALANCE_" + pay.getPaymentNo());
        paymentMapper.insert(pay);

        // ③ 订单状态改为已支付（0 待付款 → 1 已付款，取值必须落在 t_orders 状态定义内）
        R<Void> r2 = orderFeignClient.updateOrderStatus(dto.getOrderNo(), OrderStatus.PAID.getCode());
        if (r2 == null || r2.getCode() != 200) {
            throw new BizException(ResultCode.PAY_ERROR, "确认订单失败");
        }

        log.info("余额支付成功: orderNo={}, amount={}, paymentNo={}",
                dto.getOrderNo(), dto.getAmount(), pay.getPaymentNo());
        return pay.getPaymentNo();
    }
}
