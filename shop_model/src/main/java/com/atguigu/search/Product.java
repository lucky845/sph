package com.atguigu.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 这个类就是我们要存储的商品数据，将商品数据放入index,type
 * index,type 如何指定。通过spring-boot-starter-data-elasticsearch 需要添加注解表明
 * index=goods type=info shards = 分片 replicas =副本 | 保证es 查询效率，提高负载均衡用！
 */
@Data
@Document(indexName = "product", type = "info", shards = 3, replicas = 2)
public class Product {
    // 商品Id
    @Id
    private Long id;

    // 商品的默认图片
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;

    // 商品名称
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String productName;

    // 商品价格
    @Field(type = FieldType.Double)
    private Double price;

    // 创建时间
    @Field(type = FieldType.Date)
    private Date createTime; // 新品

    // 品牌Id
    @Field(type = FieldType.Long)
    private Long brandId;

    // 品牌名称
    @Field(type = FieldType.Keyword)
    private String brandName;

    // 品牌logo
    @Field(type = FieldType.Keyword)
    private String brandLogoUrl;

    @Field(type = FieldType.Long)
    private Long category1Id;

    @Field(type = FieldType.Keyword)
    private String category1Name;

    @Field(type = FieldType.Long)
    private Long category2Id;

    @Field(type = FieldType.Keyword)
    private String category2Name;

    @Field(type = FieldType.Long)
    private Long category3Id;

    @Field(type = FieldType.Keyword)
    private String category3Name;

    //热度排名
    @Field(type = FieldType.Long)
    private Long hotScore = 0L;

    //平台属性集合对象
    //Nested支持嵌套查询
    @Field(type = FieldType.Nested)
    private List<SearchPlatformProperty> platformProperty;

}
