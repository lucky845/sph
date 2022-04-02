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
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 库存单元表
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sku_info")
@ApiModel(value = "SkuInfo对象", description = "库存单元表")
public class SkuInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "库存id(itemID)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品id")
    private Long productId;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    @ApiModelProperty(value = "商品规格描述")
    private String skuDesc;

    @ApiModelProperty(value = "重量")
    private BigDecimal weight;

    @ApiModelProperty(value = "品牌(冗余)")
    private Long brandId;

    @ApiModelProperty(value = "三级分类id（冗余)")
    private Long category3Id;

    @ApiModelProperty(value = "默认显示图片(冗余)")
    private String skuDefaultImg;

    @ApiModelProperty(value = "是否销售（1：是 0：否）")
    private Integer isSale;

    @TableField(exist = false)
    List<SkuImage> skuImageList;

    @TableField(exist = false)
    List<SkuPlatformPropertyValue> skuPlatformPropertyValueList;

    @TableField(exist = false)
    List<SkuSalePropertyValue> skuSalePropertyValueList;

}
