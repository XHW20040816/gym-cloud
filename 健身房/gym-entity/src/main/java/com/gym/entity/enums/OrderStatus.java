package com.gym.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(value = "订单状态")
public enum OrderStatus {

    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REFUNDED(5, "已退款");

    @EnumValue
    @ApiModelProperty(value = "状态码")
    private final Integer code;

    @ApiModelProperty(value = "描述")
    private final String desc;
}
