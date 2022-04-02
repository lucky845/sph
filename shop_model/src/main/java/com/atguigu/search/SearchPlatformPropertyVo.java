package com.atguigu.search;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// 平台属性相关对象
@Data
public class SearchPlatformPropertyVo implements Serializable {

    // 平台属性Id
    private Long propertyKeyId;
    //当前属性值的集合
    private List<String> propertyValueList = new ArrayList<>();
    //属性名称
    private String propertyKey;
}

