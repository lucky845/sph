package com.atguigu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author lucky845
 * @since 2021-03-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("base_category_view")
@ApiModel(value = "BaseCategoryView对象", description = "VIEW")
public class BaseCategoryView implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编号")
    private Long id;

    @ApiModelProperty(value = "编号")
    private Long category1Id;

    @ApiModelProperty(value = "分类名称")
    private String category1Name;

    @ApiModelProperty(value = "编号")
    private Long category2Id;

    @ApiModelProperty(value = "二级分类名称")
    private String category2Name;

    @ApiModelProperty(value = "编号")
    private Long category3Id;

    @ApiModelProperty(value = "三级分类名称")
    private String category3Name;


}
