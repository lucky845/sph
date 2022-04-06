package com.atguigu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * spu销售属性值
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_sale_property_value")
@ApiModel(value="ProductSalePropertyValue对象", description="spu销售属性值")
public class ProductSalePropertyValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "销售属性值编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品id")
    private Long productId;

    @ApiModelProperty(value = "销售属性id")
    private Long salePropertyKeyId;

    @ApiModelProperty(value = "销售属性名称(冗余)")
    private String salePropertyKeyName;

    @ApiModelProperty(value = "销售属性值名称")
    private String salePropertyValueName;


}
