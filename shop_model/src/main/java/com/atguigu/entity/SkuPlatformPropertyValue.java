package com.atguigu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * sku平台属性值关联表
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sku_platform_property_value")
@ApiModel(value = "SkuPlatformPropertyValue对象", description = "sku平台属性值关联表")
public class SkuPlatformPropertyValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "属性id（冗余)")
    private Long propertyKeyId;

    @ApiModelProperty(value = "属性值id")
    private Long propertyValueId;

    @ApiModelProperty(value = "skuid")
    private Long skuId;


}
