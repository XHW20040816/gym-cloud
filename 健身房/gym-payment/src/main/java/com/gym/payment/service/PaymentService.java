package com.gym.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.api.dto.PayDTO;
import com.gym.api.order.OrderFeignClient;
import com.gym.common.exception.BizException;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.enums.PayType;
import com.gym.entity.payment.Payment;
import com.gym.payment.mapper.PaymentMapper;
import com.gym.payment.strategy.PayStrategy;
import com.gym.payment.strategy.PayStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayStrategyFactory payStrategyFactory;
    private final PaymentMapper paymentMapper;

    @Autowired
    @Lazy
    private OrderFeignClient orderFeignClient;

    /**
     * 统一支付入口
     */
    public String pay(PayDTO dto) {
        if (dto == null || dto.getPayType() == null) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        PayType type = resolvePayType(dto.getPayType());
        PayStrategy strategy = payStrategyFactory.get(type);
        return strategy.pay(dto);
    }

    /**
     * Mock 支付回调（支付宝/微信共用逻辑）
     * @param outTradeNo 商户订单号（即 orderNo）
     * @param transactionId 第三方流水号
     * @param type 支付方式
     */
    @Transactional(rollbackFor = Exception.class)
    public void mockCallback(String outTradeNo, String transactionId, PayType type) {
        // 1) 查询支付记录
        Payment payment = paymentMapper.selectByOrderAndType(outTradeNo, type.getCode());
        if (payment == null) {
            throw new BizException(ResultCode.PAY_ERROR, "支付记录不存在");
        }
        // 2) 幂等：已成功则直接返回
        if (Payment.STATUS_SUCCESS == payment.getStatus()) {
            log.info("回调幂等：支付已成功，跳过 orderNo={}, type={}", outTradeNo, type);
            return;
        }
        // 3) 只允许未支付状态 -> 成功
        if (Payment.STATUS_UNPAID != payment.getStatus()) {
            throw new BizException(ResultCode.PAY_ERROR, "支付记录状态错误");
        }

        // 4) 更新支付流水：状态成功 + 第三方流水号 + 支付时间
        Payment update = new Payment();
        update.setId(payment.getId());
        update.setStatus(Payment.STATUS_SUCCESS);
        update.setTransactionId(transactionId);
        update.setPayTime(LocalDateTime.now());
        paymentMapper.updateById(update);

        // 5) 调用订单服务更新订单状态为 PAID
        R<Void> r = orderFeignClient.updateOrderStatus(outTradeNo, OrderStatus.PAID.getCode());
        if (r == null || r.getCode() != 200) {
            throw new BizException(ResultCode.PAY_ERROR, "更新订单状态失败");
        }

        log.info("Mock回调支付成功: orderNo={}, type={}, transactionId={}", outTradeNo, type, transactionId);
    }

    /**
     * 根据订单号查询支付记录列表
     */
    public List<Payment> getPaymentByOrder(String orderNo) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderNo, orderNo)
                .orderByDesc(Payment::getCreateTime);
        return paymentMapper.selectList(wrapper);
    }

    /**
     * 我的支付记录分页
     */
    public PageResult<Payment> myPage(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        int pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int ps = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize;

        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getUserId, userId)
                .orderByDesc(Payment::getCreateTime);

        IPage<Payment> page = paymentMapper.selectPage(new Page<>(pn, ps), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal());
    }

    /**
     * 管理员全量分页
     */
    public PageResult<Payment> adminPage(Integer pageNum, Integer pageSize,
                                         Long userId, String orderNo, Integer status) {
        int pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int ps = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize;

        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Payment::getUserId, userId);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Payment::getOrderNo, orderNo.trim());
        }
        if (status != null) {
            wrapper.eq(Payment::getStatus, status);
        }
        wrapper.orderByDesc(Payment::getCreateTime);

        IPage<Payment> page = paymentMapper.selectPage(new Page<>(pn, ps), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal());
    }

    /**
     * 根据 paymentNo 查询详情（带权限校验：本人或管理员）
     */
    public Payment detail(String paymentNo, Long currentUserId, String role) {
        if (paymentNo == null || paymentNo.trim().isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo.trim());
        Payment pay = paymentMapper.selectOne(wrapper);
        if (pay == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        // 权限校验：管理员或本人
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isAdmin && (currentUserId == null || !currentUserId.equals(pay.getUserId()))) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return pay;
    }

    /**
     * Integer code -> PayType 枚举
     */
    private PayType resolvePayType(Integer code) {
        for (PayType t : PayType.values()) {
            if (t.getCode().equals(code)) {
                return t;
            }
        }
        throw new BizException(ResultCode.PAY_ERROR, "不支持的支付方式编码: " + code);
    }
}
