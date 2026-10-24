package com.gym.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "支付DTO")
public class PayDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "支付方式（1余额 2支付宝 3微信）")
    private Integer payType;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal amount;
}
