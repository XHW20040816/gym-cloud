package com.gym.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "创建订单DTO")
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID（从请求头X-User-Id获取）")
    private Long userId;

    @ApiModelProperty(value = "配送信息/地址")
    private String deliveryInfo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    private List<OrderItemDTO> items;
}
