package com.gym.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "订单明细DTO")
public class OrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品/课程/餐饮ID")
    private Long goodsId;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "明细类型：1商品 2课程 3餐饮")
    private Integer itemType;
}
