package com.gym.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(value = "会员卡类型")
public enum CardType {

    TIMES(0, "次卡"),
    MONTH(1, "月卡"),
    QUARTER(3, "季卡"),
    YEAR(12, "年卡");

    @EnumValue
    @ApiModelProperty(value = "编码（月份数，0表示次卡）")
    private final Integer code;

    @ApiModelProperty(value = "描述")
    private final String desc;
}
