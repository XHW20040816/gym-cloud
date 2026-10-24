package com.gym.entity.member;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gym.entity.enums.CardType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_member_card")
@ApiModel(value = "会员卡实体")
public class MemberCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "卡名")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "卡类型")
    @TableField(value = "type", typeHandler = com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.class)
    private CardType type;

    @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "有效期天数")
    @TableField("days")
    private Integer days;

    @ApiModelProperty(value = "次卡次数")
    @TableField("times")
    private Integer times;

    @ApiModelProperty(value = "描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
