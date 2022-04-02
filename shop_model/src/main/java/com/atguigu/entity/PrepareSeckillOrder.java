package com.atguigu.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PrepareSeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    private SeckillProduct seckillProduct;

    private Integer buyNum;

    private String prepareOrderCode;
}
