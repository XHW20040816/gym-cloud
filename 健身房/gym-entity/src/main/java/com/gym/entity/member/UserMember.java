package com.gym.entity.member;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_user_member")
@ApiModel(value = "用户会员实体")
public class UserMember implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_EXPIRED = 2;
    public static final int STATUS_FROZEN = 0;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "会员卡ID")
    @TableField("card_id")
    private Long cardId;

    @ApiModelProperty(value = "开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "剩余次数")
    @TableField("remain_times")
    private Integer remainTimes;

    @ApiModelProperty(value = "状态：0冻结 1正常 2已过期")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
