package com.gym.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "系统异常"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    STOCK_NOT_ENOUGH(600, "库存不足"),
    REPEAT_SECKILL(601, "已达限购数量"),
    SECKILL_END(602, "秒杀活动已结束"),
    PAY_ERROR(610, "支付失败"),
    ORDER_NOT_EXIST(620, "订单不存在");

    private final Integer code;
    private final String msg;
}
