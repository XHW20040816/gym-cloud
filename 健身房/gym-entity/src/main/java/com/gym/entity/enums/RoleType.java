package com.gym.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(value = "角色类型")
public enum RoleType {

    USER("USER", "普通用户"),
    COACH("COACH", "教练"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    @ApiModelProperty(value = "编码")
    private final String code;

    @ApiModelProperty(value = "描述")
    private final String desc;
}
