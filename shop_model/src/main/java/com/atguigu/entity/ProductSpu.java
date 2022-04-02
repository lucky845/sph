package com.atguigu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_spu")
@ApiModel(value = "ProductSpu对象", description = "商品表")
public class ProductSpu implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品描述(后台简述）")
    private String description;

    @ApiModelProperty(value = "三级分类id")
    private Long category3Id;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    // 销售属性集合
    @TableField(exist = false)
    private List<ProductSalePropertyKey> salePropertyKeyList;

    @TableField(exist = false)
    private List<ProductImage> productImageList;


}
