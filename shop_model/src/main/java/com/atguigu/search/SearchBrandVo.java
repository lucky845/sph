package com.atguigu.search;

import lombok.Data;

import java.io.Serializable;

// 品牌数据
@Data
public class SearchBrandVo implements Serializable {
    //当前属性值的所有值
    private Long brandId;
    //属性名称
    private String brandName;
    //图片名称
    private String brandLogoUrl;
}

