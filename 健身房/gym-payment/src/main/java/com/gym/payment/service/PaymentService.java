package com.gym.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.api.dto.PayDTO;
import com.gym.api.order.OrderFeignClient;
import com.gym.api.user.UserFeignClient;
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
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayStrategyFactory payStrategyFactory;
    private final PaymentMapper paymentMapper;

    @Autowired
    @Lazy
    private OrderFeignClient orderFeignClient;

    @Autowired
    @Lazy
    private UserFeignClient userFeignClient;

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
                                         Long userId, String orderNo, String paymentNo,
                                         Integer payType, Integer status) {
        int pn = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int ps = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize;

        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Payment::getUserId, userId);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Payment::getOrderNo, orderNo.trim());
        }
        if (paymentNo != null && !paymentNo.trim().isEmpty()) {
            wrapper.like(Payment::getPaymentNo, paymentNo.trim());
        }
        if (payType != null) {
            wrapper.eq(Payment::getPayType, resolvePayType(payType));
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
     * 管理员退款：仅支付成功的记录可退，退款后同步订单状态为已退款；
     * 余额支付同时退还用户余额（跨库，Seata 全局事务保证一致）
     */
    @GlobalTransactional(rollbackFor = Exception.class, name = "payment-refund-tx")
    @Transactional(rollbackFor = Exception.class)
    public void refund(String paymentNo) {
        if (paymentNo == null || paymentNo.trim().isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getPaymentNo, paymentNo.trim());
        Payment pay = paymentMapper.selectOne(wrapper);
        if (pay == null) {
            throw new BizException(ResultCode.NOT_FOUND, "支付记录不存在");
        }
        if (pay.getStatus() == null || pay.getStatus() != Payment.STATUS_SUCCESS) {
            throw new BizException(ResultCode.PAY_ERROR, "仅支付成功的记录可退款");
        }

        // ① 余额支付：先退还余额（跨库，Seata 全局事务保证一致）
        if (PayType.BALANCE.equals(pay.getPayType())) {
            R<Void> r1 = userFeignClient.addBalance(pay.getUserId(), pay.getAmount());
            if (r1 == null || r1.getCode() != 200) {
                String msg = (r1 != null && r1.getMsg() != null) ? r1.getMsg() : "退还余额失败";
                throw new BizException(ResultCode.PAY_ERROR, msg);
            }
        }

        // ② 更新流水状态为已退款
        Payment update = new Payment();
        update.setId(pay.getId());
        update.setStatus(Payment.STATUS_REFUNDED);
        paymentMapper.updateById(update);

        // ③ 订单状态改为已退款
        R<Void> r = orderFeignClient.updateOrderStatus(pay.getOrderNo(), OrderStatus.REFUNDED.getCode());
        if (r == null || r.getCode() != 200) {
            throw new BizException(ResultCode.PAY_ERROR, "更新订单状态失败");
        }
        log.info("退款成功 paymentNo={}, orderNo={}", pay.getPaymentNo(), pay.getOrderNo());
    }

    /**
     * 支付统计：总量、笔数、退款、成功率与近30天按日趋势
     */
    public Map<String, Object> statistics() {
        BigDecimal totalAmount = paymentMapper.sumSuccessAmount();
        long successCount = paymentMapper.countSuccess();
        BigDecimal refundAmount = paymentMapper.sumRefundAmount();
        long failCount = paymentMapper.countFail();
        long total = successCount + failCount;
        double successRate = total == 0 ? 0 : Math.round(successCount * 1000.0 / total) / 10.0;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalAmount", totalAmount == null ? BigDecimal.ZERO : totalAmount);
        data.put("totalCount", successCount);
        data.put("refundAmount", refundAmount == null ? BigDecimal.ZERO : refundAmount);
        data.put("successRate", successRate);

        LocalDateTime start = LocalDateTime.now().minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Map<String, Object>> rows = paymentMapper.sumAmountGroupByDay(
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Map<String, BigDecimal> dayMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object d = row.get("d");
            Object amt = row.get("amt");
            if (d != null) {
                dayMap.put(String.valueOf(d), amt == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(amt)));
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDateTime day = start.plusDays(i);
            String key = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.format(DateTimeFormatter.ofPattern("MM-dd")));
            point.put("amount", dayMap.getOrDefault(key, BigDecimal.ZERO));
            trend.add(point);
        }
        data.put("trend", trend);
        return data;
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
