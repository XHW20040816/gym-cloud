package com.gym.entity.food;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gym.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_food")
@ApiModel(value = "餐饮实体")
public class Food extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "餐品名")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "图片")
    @TableField("image")
    private String image;

    @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "卡路里（千卡）")
    @TableField("calories")
    private Double calories;

    @ApiModelProperty(value = "蛋白质（克）")
    @TableField("protein")
    private Double protein;

    @ApiModelProperty(value = "碳水化合物（克）")
    @TableField("carbs")
    private Double carbs;

    @ApiModelProperty(value = "脂肪（克）")
    @TableField("fat")
    private Double fat;

    @ApiModelProperty(value = "描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private Integer status;
}
