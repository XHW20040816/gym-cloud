package com.gym.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 注册请求 DTO。
 */
@Data
@ApiModel("注册请求")
public class RegisterDTO {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("角色：USER/COACH/ADMIN（默认 USER）")
    private String role;

    @ApiModelProperty("昵称（选填）")
    private String nickname;

    @ApiModelProperty("手机号（选填）")
    private String phone;

    @ApiModelProperty("邮箱（选填）")
    private String email;

    @ApiModelProperty("管理员邀请码（仅注册 ADMIN 时必填）")
    private String inviteCode;
}
