package com.itbaizhan.shopping_common.pojo;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 在ES中存储的商品实体类
 */
@Document(indexName = "goods",createIndex = false)
@Data
public class GoodsES implements Serializable {
    @Field
    private Long id; // 商品id
    @Field
    private String goodsName; // 商品名称
    @Field
    private String caption; // 副标题
    @Field
    private BigDecimal price; // 价格
    @Field
    private String headerPic; // 头图
    @Field
    private String brand; // 品牌名称
    //这个注解是用于自动补全
    //当用户输入关键词时，提供类似搜索引擎的"联想词"或"自动补全"功能。
    @CompletionField
    private List<String> tags; // 关键字，存储多个标签、关键字、热词
    @Field
    private List<String> productType; // 类目名
    @Field
    private Map<String,List<String>> specification; // 规格,键为规格项,值为规格值
}
