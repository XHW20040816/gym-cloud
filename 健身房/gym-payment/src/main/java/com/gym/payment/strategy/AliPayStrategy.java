package com.gym.payment.strategy;

import com.gym.api.dto.PayDTO;
import com.gym.common.constant.RedisConstants;
import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import com.gym.common.util.SnowflakeIdUtil;
import com.gym.entity.enums.PayType;
import com.gym.entity.payment.Payment;
import com.gym.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AliPayStrategy implements PayStrategy {

    private final RedissonClient redissonClient;
    private final PaymentMapper paymentMapper;
    private final SnowflakeIdUtil idUtil;
    private final StringRedisTemplate stringRedisTemplate;

    /** 支付宝预支付单 Redis 前缀 */
    private static final String ALIPAY_PAY_KEY = "alipay:pay:";
    /** 预支付单 TTL 15 分钟 */
    private static final long ALIPAY_TTL_MIN = 15L;

    @Override
    public PayType getType() {
        return PayType.ALIPAY;
    }

    @Override
    public String pay(PayDTO dto) {
        // 1) 幂等：先查是否已有支付单（成功或待支付均可）
        Payment exist = paymentMapper.selectByOrderAndType(dto.getOrderNo(), PayType.ALIPAY.getCode());
        if (exist != null) {
            if (Payment.STATUS_SUCCESS == exist.getStatus()) {
                return exist.getPaymentNo();
            }
            // 待支付状态则返回已有的 paymentNo，前端可重新拉取支付链接
            return exist.getPaymentNo();
        }

        // 2) 分布式锁防重复创建预支付单
        RLock lock = redissonClient.getLock(RedisConstants.PAY_LOCK_KEY + dto.getOrderNo() + ":alipay");
        try {
            if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                throw new BizException(ResultCode.PAY_ERROR.getCode(), "请勿重复提交");
            }

            // 3) 双重检查幂等
            exist = paymentMapper.selectByOrderAndType(dto.getOrderNo(), PayType.ALIPAY.getCode());
            if (exist != null) {
                return exist.getPaymentNo();
            }

            // 4) 生成支付宝预支付单（Mock）：模拟 alipay.trade.page.pay 返回支付链接/二维码
            String paymentNo = idUtil.nextIdStr();
            String mockPrepayId = "ALIPAY_" + paymentNo;
            String mockPayLink = "https://mock.alipay.com/gateway.do?prepay_id=" + mockPrepayId
                    + "&qrcode=https://qr.alipay.com/mock/" + mockPrepayId;

            // 5) 预支付信息存入 Redis，15 分钟过期
            stringRedisTemplate.opsForValue().set(
                    ALIPAY_PAY_KEY + dto.getOrderNo(),
                    mockPayLink,
                    ALIPAY_TTL_MIN,
                    TimeUnit.MINUTES
            );

            // 6) 插入支付流水，status=0(未支付)，transactionId=预支付单号
            Payment pay = new Payment();
            pay.setPaymentNo(paymentNo);
            pay.setOrderNo(dto.getOrderNo());
            pay.setUserId(dto.getUserId());
            pay.setAmount(dto.getAmount());
            pay.setPayType(PayType.ALIPAY);
            pay.setStatus(Payment.STATUS_UNPAID);
            pay.setCreateTime(LocalDateTime.now());
            pay.setTransactionId(mockPrepayId);
            paymentMapper.insert(pay);

            log.info("支付宝预支付单创建成功: orderNo={}, paymentNo={}, payLink={}",
                    dto.getOrderNo(), paymentNo, mockPayLink);
            return paymentNo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.PAY_ERROR, "创建支付单被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
