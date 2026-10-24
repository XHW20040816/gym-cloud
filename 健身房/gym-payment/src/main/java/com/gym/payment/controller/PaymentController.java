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
        return R.ok(result);
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
            @ApiParam("流水号模糊(可选)") @RequestParam(required = false) String paymentNo,
            @ApiParam("支付方式 1余额 2支付宝 3微信(可选)") @RequestParam(required = false) Integer payType,
            @ApiParam("状态 0未支付 1成功 2失败 3已退款(可选)") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        PageResult<Payment> page = paymentService.adminPage(pageNum, pageSize, userId, orderNo, paymentNo, payType, status);
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

    @ApiOperation("【ADMIN】支付退款")
    @PostMapping("/{paymentNo}/refund")
    public R<Void> refund(@ApiParam("支付流水号") @PathVariable String paymentNo,
                          HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        paymentService.refund(paymentNo);
        return R.ok();
    }

    @ApiOperation("【ADMIN】支付统计")
    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return R.fail(ResultCode.FORBIDDEN);
        }
        return R.ok(paymentService.statistics());
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
