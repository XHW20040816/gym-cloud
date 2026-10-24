package com.gym.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(value = "用户状态")
public enum UserStatus {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    @EnumValue
    @ApiModelProperty(value = "状态码")
    private final Integer code;

    @ApiModelProperty(value = "描述")
    private final String desc;
}
