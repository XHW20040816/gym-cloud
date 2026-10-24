package com.gym.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("t_order_item")
@ApiModel(value = "订单明细实体")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int ITEM_TYPE_GOODS = 1;
    public static final int ITEM_TYPE_COURSE = 2;
    public static final int ITEM_TYPE_FOOD = 3;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "订单号（存orderNo）")
    @TableField("order_id")
    private String orderId;

    @ApiModelProperty(value = "商品/课程/餐饮ID")
    @TableField("goods_id")
    private Long goodsId;

    @ApiModelProperty(value = "名称")
    @TableField("goods_name")
    private String goodsName;

    @ApiModelProperty(value = "封面图")
    @TableField("goods_cover")
    private String goodsCover;

    @ApiModelProperty(value = "单价")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "数量")
    @TableField("quantity")
    private Integer quantity;

    @ApiModelProperty(value = "小计")
    @TableField("subtotal")
    private BigDecimal subtotal;

    @ApiModelProperty(value = "明细类型：1商品 2课程 3餐饮")
    @TableField("item_type")
    private Integer itemType;
}
