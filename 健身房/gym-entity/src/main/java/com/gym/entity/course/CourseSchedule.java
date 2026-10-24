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

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course_schedule")
@ApiModel(value = "课程排期实体")
public class CourseSchedule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "课程ID")
    @TableField("course_id")
    private Long courseId;

    @ApiModelProperty(value = "上课日期")
    @TableField("class_date")
    private LocalDate classDate;

    @ApiModelProperty(value = "开始时间")
    @TableField("start_time")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间")
    @TableField("end_time")
    private LocalTime endTime;

    @ApiModelProperty(value = "容量")
    @TableField("capacity")
    private Integer capacity;

    @ApiModelProperty(value = "已报名人数")
    @TableField("enrollment_count")
    private Integer enrollmentCount;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private Integer status;
}
