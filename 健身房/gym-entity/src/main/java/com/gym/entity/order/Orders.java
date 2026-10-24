package com.gym.entity.order;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gym.entity.enums.OrderStatus;
import com.gym.entity.enums.PayType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_orders")
@ApiModel(value = "订单实体")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "订单号（唯一）")
    @TableField("order_no")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "实付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "订单状态")
    @TableField(value = "status", typeHandler = com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.class)
    private OrderStatus status;

    @ApiModelProperty(value = "支付方式")
    @TableField(value = "pay_type", typeHandler = com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.class)
    private PayType payType;

    @ApiModelProperty(value = "支付时间")
    @TableField("pay_time")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "配送信息")
    @TableField("delivery_info")
    private String deliveryInfo;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
