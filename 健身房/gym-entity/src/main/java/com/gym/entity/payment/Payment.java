package com.gym.entity.payment;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gym.entity.enums.PayType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_payment")
@ApiModel(value = "支付记录实体")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_UNPAID = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAIL = 2;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "支付流水号（唯一）")
    @TableField("payment_no")
    private String paymentNo;

    @ApiModelProperty(value = "订单号")
    @TableField("order_no")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "支付金额")
    @TableField("amount")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付方式")
    @TableField(value = "pay_type", typeHandler = com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.class)
    private PayType payType;

    @ApiModelProperty(value = "状态：0未支付 1成功 2失败")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "第三方流水号")
    @TableField("transaction_id")
    private String transactionId;

    @ApiModelProperty(value = "支付时间")
    @TableField("pay_time")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
