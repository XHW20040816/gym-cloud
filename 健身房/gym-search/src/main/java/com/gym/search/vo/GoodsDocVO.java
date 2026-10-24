package com.gym.search.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("商品ES搜索结果VO")
public class GoodsDocVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID")
    private Long id;

    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "商品名称(高亮后)")
    private String name;

    @ApiModelProperty(value = "封面")
    private String cover;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "描述(高亮后)")
    private String description;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
