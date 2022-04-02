package com.atguigu.search;

import lombok.Data;

// 封装查询条件
@Data
public class SearchParam {
    //分类id
    private Long category1Id;
    private Long category2Id;
    private Long category3Id;
    //品牌 brandName=
    private String brandName;
    //检索的关键字
    private String keyword;

    // 排序规则 1:综合排序/热点  2:价格
    private String order = "";

    //平台属性 页面提交的数组
    private String[] props;
    //分页信息
    private Integer pageNo = 1;
    private Integer pageSize = 3;


}
