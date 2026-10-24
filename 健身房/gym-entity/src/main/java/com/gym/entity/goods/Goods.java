package com.gym.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.gym.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_goods")
@ApiModel(value = "商品实体")
public class Goods extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "分类ID")
    @TableField("category_id")
    private Long categoryId;

    @ApiModelProperty(value = "商品名")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "封面图")
    @TableField("cover")
    private String cover;

    @ApiModelProperty(value = "图片集")
    @TableField("images")
    private String images;

    @ApiModelProperty(value = "库存")
    @TableField("stock")
    private Integer stock;

    @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "销量")
    @TableField("sales")
    private Integer sales;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "乐观锁版本号")
    @Version
    @TableField("version")
    private Integer version;
}
