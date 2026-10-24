package com.gym.search.document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "goods_index", createIndex = false)
@Setting(shards = 1, replicas = 0)
@ApiModel("商品ES文档")
public class GoodsDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ApiModelProperty(value = "商品ID")
    private Long id;

    @ApiModelProperty(value = "分类ID")
    @Field(type = FieldType.Keyword)
    private Long categoryId;

    @ApiModelProperty(value = "商品名称, ik_max_word 分词")
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @ApiModelProperty(value = "封面")
    @Field(type = FieldType.Keyword, index = false)
    private String cover;

    @ApiModelProperty(value = "库存")
    @Field(type = FieldType.Integer)
    private Integer stock;

    @ApiModelProperty(value = "价格")
    @Field(type = FieldType.Double)
    private BigDecimal price;

    @ApiModelProperty(value = "描述, ik_smart 分词")
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String description;

    @ApiModelProperty(value = "销量")
    @Field(type = FieldType.Integer)
    private Integer sales;

    @ApiModelProperty(value = "状态 1上架0下架")
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 必须用不带时区偏移的自定义格式：
     * - date_optional_time 在本版本写入会退化成 yyyy-MM-dd，读回无法转 LocalDateTime；
     * - date_time 要求 OffsetSeconds，LocalDateTime 不支持。
     */
    @ApiModelProperty(value = "创建时间")
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "名称自动补全字段")
    @CompletionField(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", maxInputLength = 50)
    private Completion suggest;
}
