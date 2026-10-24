package com.gym.entity.seckill;

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
@TableName("t_seckill_goods")
@ApiModel(value = "秒杀商品实体")
public class SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "活动ID")
    @TableField("activity_id")
    private Long activityId;

    @ApiModelProperty(value = "商品ID")
    @TableField("goods_id")
    private Long goodsId;

    @ApiModelProperty(value = "秒杀价")
    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @ApiModelProperty(value = "库存")
    @TableField("stock")
    private Integer stock;

    @ApiModelProperty(value = "已售数量")
    @TableField("sold")
    private Integer sold;

    @ApiModelProperty(value = "排序")
    @TableField("sort")
    private Integer sort;
}
