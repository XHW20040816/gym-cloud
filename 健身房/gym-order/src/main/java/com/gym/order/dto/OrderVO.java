package com.gym.order.dto;

import com.gym.entity.order.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 管理端订单列表聚合 VO：订单主表 + 用户信息 + 首个商品明细摘要
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "管理端订单聚合视图")
public class OrderVO extends Orders {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "下单用户昵称")
    private String nickname;

    @ApiModelProperty(value = "下单用户手机号")
    private String phone;

    @ApiModelProperty(value = "首个商品名称")
    private String goodsName;

    @ApiModelProperty(value = "首个商品封面")
    private String goodsCover;

    @ApiModelProperty(value = "首个商品数量")
    private Integer quantity;

    @ApiModelProperty(value = "首个商品单价")
    private BigDecimal price;
}
