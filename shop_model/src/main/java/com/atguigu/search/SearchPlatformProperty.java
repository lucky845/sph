package com.atguigu.search;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class SearchPlatformProperty {
    //平台属性Id
    @Field(type = FieldType.Long)
    private Long propertyKeyId;
    //平台属性值
    @Field(type = FieldType.Keyword)
    private String propertyValue;
    //平台属性名称
    @Field(type = FieldType.Keyword)
    private String propertyKey;
}
