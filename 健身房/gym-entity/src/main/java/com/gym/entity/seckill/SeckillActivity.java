package com.gym.entity.seckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gym.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_seckill_activity")
@ApiModel(value = "秒杀活动实体")
public class SeckillActivity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_NOT_START = 0;
    public static final int STATUS_ONGOING = 1;
    public static final int STATUS_ENDED = 2;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "活动名称")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "封面图")
    @TableField("cover")
    private String cover;

    @ApiModelProperty(value = "开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "状态：0未开始 1进行中 2已结束")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "总库存")
    @TableField("total_stock")
    private Integer totalStock;

    @ApiModelProperty(value = "每人限购数量（默认1）")
    @TableField("limit_count")
    private Integer limitCount;
}
