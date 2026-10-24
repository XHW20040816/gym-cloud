package com.gym.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gym.entity.BaseEntity;
import com.gym.entity.enums.RoleType;
import com.gym.entity.enums.UserStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
@ApiModel(value = "用户实体")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户名（唯一）")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "密码（BCrypt加密）")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty(value = "手机号")
    @TableField("phone")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty(value = "角色")
    @TableField(value = "role", typeHandler = com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.class)
    private RoleType role;

    @ApiModelProperty(value = "状态")
    @TableField(value = "status", typeHandler = com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.class)
    private UserStatus status;

    @ApiModelProperty(value = "余额")
    @TableField("balance")
    private BigDecimal balance;

    @ApiModelProperty(value = "积分")
    @TableField("point")
    private Integer point;
}
