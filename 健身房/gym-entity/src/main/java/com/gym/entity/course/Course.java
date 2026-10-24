package com.gym.entity.course;

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
@TableName("t_course")
@ApiModel(value = "课程实体")
public class Course extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "课程名称")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "教练ID")
    @TableField("coach_id")
    private Long coachId;

    @ApiModelProperty(value = "分类")
    @TableField("category")
    private String category;

    @ApiModelProperty(value = "容量")
    @TableField("capacity")
    private Integer capacity;

    @ApiModelProperty(value = "时长（分钟）")
    @TableField("duration")
    private Integer duration;

    @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private Integer status;
}
