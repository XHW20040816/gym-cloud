package com.gym.payment.controller;

import com.gym.api.dto.PayDTO;
import com.gym.common.exception.BizException;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.enums.PayType;
import com.gym.entity.payment.Payment;
import com.gym.payment.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "支付管理")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String ALIPAY_PAY_KEY = "alipay:pay:";
    private static final String WX_PAY_KEY = "wxpay:pay:";

    // ============== 公共支付接口 ==============

    @ApiOperation("发起支付")
    @PostMapping("/pay")
    public R<Map<String, Object>> pay(@RequestBody PayDTO dto, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        dto.setUserId(Long.valueOf(userIdStr));

        String paymentNo = paymentService.pay(dto);
        Map<String, Object> result = new HashMap<>(8);
        result.put("paymentNo", paymentNo);
        result.put("payType", dto.getPayType());

        // 支付宝/微信返回预支付链接，余额支付没有 payLink
        if (PayType.ALIPAY.getCode().equals(dto.getPayType())) {
            String link = stringRedisTemplate.opsForValue().get(ALIPAY_PAY_KEY + dto.getOrderNo());
            result.put("payLink", link);
        } else if (PayType.WECHAT.getCode().equals(dto.getPayType())) {
            String link = stringRedisTemplate.opsForValue().get(WX_PAY_KEY + dto.getOrderNo());
            result.put("payLink", link);
        } else {
            result.put("payLink", null);
        }
        return R.ok(result);
    }

    // ============== Mock 回调接口（内部模拟，不做验签） ==============

    @ApiOperation("【Mock】支付宝支付回调")
    @PostMapping("/callback/alipay")
    public R<Void> alipayCallback(
            @ApiParam("商户订单号(orderNo)") @RequestParam String outTradeNo,
            @ApiParam("支付宝交易号") @RequestParam String tradeNo) {
        log.info("【Mock】支付宝回调: outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);
        paymentService.mockCallback(outTradeNo, tradeNo, PayType.ALIPAY);
        return R.ok();
    }

    @ApiOperation("【Mock】微信支付回调")
    @PostMapping("/callback/wechat")
    public R<Void> wechatCallback(
            @ApiParam("商户订单号(orderNo)") @RequestParam String outTradeNo,
            @ApiParam("微信支付单号") @RequestParam String transactionId) {
        log.info("【Mock】微信回调: outTradeNo={}, transactionId={}", outTradeNo, transactionId);
        paymentService.mockCallback(outTradeNo, transactionId, PayType.WECHAT);
        return R.ok();
    }

    // ============== 用户接口 ==============

    @ApiOperation("我的支付记录（分页）")
    @GetMapping("/my")
    public R<PageResult<Payment>> my(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        PageResult<Payment> page = paymentService.myPage(userId, pageNum, pageSize);
        return R.ok(page);
    }

    @ApiOperation("支付详情")
    @GetMapping("/detail/{paymentNo}")
    public R<Payment> detail(@PathVariable String paymentNo, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        Long userId = (userIdStr != null && !userIdStr.isEmpty()) ? Long.valueOf(userIdStr) : null;
        Payment pay = paymentService.detail(paymentNo, userId, role);
        return R.ok(pay);
    }

    // ============== 管理员接口 ==============

    @ApiOperation("【ADMIN】全量支付记录分页")
    @GetMapping("/page")
    public R<PageResult<Payment>> adminPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("用户ID(可选)") @RequestParam(required = false) Long userId,
            @ApiParam("订单号模糊(可选)") @RequestParam(required = false) String orderNo,
            @ApiParam("状态 0未支付 1成功 2失败(可选)") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        PageResult<Payment> page = paymentService.adminPage(pageNum, pageSize, userId, orderNo, status);
        return R.ok(page);
    }

    @ApiOperation("根据订单号查询所有支付记录")
    @GetMapping("/order/{orderNo}")
    public R<List<Payment>> listByOrder(@PathVariable String orderNo, HttpServletRequest request) {
        // 鉴权：管理员或本人（通过订单归属）
        String role = request.getHeader("X-User-Role");
        String userIdStr = request.getHeader("X-User-Id");
        List<Payment> list = paymentService.getPaymentByOrder(orderNo);
        if (list == null || list.isEmpty()) {
            return R.ok(list);
        }
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Long uid = (userIdStr != null && !userIdStr.isEmpty()) ? Long.valueOf(userIdStr) : null;
            Payment first = list.get(0);
            if (uid == null || !uid.equals(first.getUserId())) {
                return R.fail(ResultCode.FORBIDDEN);
            }
        }
        return R.ok(list);
    }

    // ============== 测试用接口：Seata 回滚验证 ==============

    @ApiOperation("【测试】Seata 分布式事务回滚验证（用余额不足的金额调用即可触发全局回滚）")
    @PostMapping("/test/rollback")
    public R<String> testRollback(@RequestBody PayDTO dto, HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        dto.setUserId(Long.valueOf(userIdStr));
        if (dto.getPayType() == null) {
            dto.setPayType(PayType.BALANCE.getCode());
        }

        // 传入大于用户余额的 amount，gym-user 扣减余额会失败 → BalancePayTxService 抛异常 → Seata 回滚
        log.warn("【Seata 回滚测试】余额支付 orderNo={}, amount={}", dto.getOrderNo(), dto.getAmount());
        try {
            String paymentNo = paymentService.pay(dto);
            return R.ok("支付成功，paymentNo=" + paymentNo + "（未触发回滚，请用大于余额的金额重试）");
        } catch (BizException e) {
            return R.fail(e.getCode(), "支付失败（已触发全局回滚，请核对：订单状态仍为 UNPAID、余额未扣、t_payment 无记录）: " + e.getMsg());
        }
    }
}
