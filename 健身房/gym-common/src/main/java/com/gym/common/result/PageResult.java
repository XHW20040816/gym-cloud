package com.gym.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "分页返回结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总条数")
    private Long total;

    @ApiModelProperty(value = "数据列表")
    private List<T> rows;

    public static <T> PageResult<T> of(List<T> rows, long total) {
        return new PageResult<>(total, rows);
    }
}
