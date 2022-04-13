package com.atguigu.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.entity.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
public interface BaseCategoryViewService extends IService<BaseCategoryView> {

    /**
     * 查询首页分类信息
     */
    List<JSONObject> getIndexCategory();

}
