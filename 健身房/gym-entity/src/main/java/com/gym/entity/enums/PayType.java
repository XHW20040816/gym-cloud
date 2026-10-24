package com.gym.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(value = "支付类型")
public enum PayType {

    BALANCE(1, "余额支付"),
    ALIPAY(2, "支付宝"),
    WECHAT(3, "微信支付");

    @EnumValue
    @ApiModelProperty(value = "编码")
    private final Integer code;

    @ApiModelProperty(value = "描述")
    private final String desc;
}
