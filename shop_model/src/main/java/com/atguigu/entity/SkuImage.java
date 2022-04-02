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
 * 库存单元图片表
 * </p>
 *
 * @author lucky845
 * @since 2022-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sku_image")
@ApiModel(value = "SkuImage对象", description = "库存单元图片表")
public class SkuImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "sku的id")
    private Long skuId;

    @ApiModelProperty(value = "图片名称（冗余）")
    private String imageName;

    @ApiModelProperty(value = "图片路径(冗余)")
    private String imageUrl;

    @ApiModelProperty(value = "商品图片id")
    private Long productImageId;

    @ApiModelProperty(value = "是否默认")
    private String isDefault;


}
