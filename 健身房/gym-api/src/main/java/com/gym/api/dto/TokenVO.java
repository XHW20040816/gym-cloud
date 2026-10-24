package com.gym.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "令牌VO")
public class TokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "访问令牌")
    private String access_token;

    @ApiModelProperty(value = "刷新令牌")
    private String refresh_token;

    @ApiModelProperty(value = "令牌类型", example = "Bearer")
    @Builder.Default
    private String token_type = "Bearer";

    @ApiModelProperty(value = "过期时间（秒）")
    private Integer expires_in;
}
