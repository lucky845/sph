package com.atguigu.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSeckillSkuInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;

    private String userId;
}
