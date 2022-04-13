package com.atguigu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.mapper.BaseCategoryViewMapper;
import com.atguigu.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Service
public class BaseCategoryViewServiceImpl extends ServiceImpl<BaseCategoryViewMapper, BaseCategoryView> implements BaseCategoryViewService {

    /**
     * 查询首页分类信息
     */
    @Override
    public List<JSONObject> getIndexCategory() {
        // 1. 查询所有的分类信息
        List<BaseCategoryView> allCategoryViewList = baseMapper.selectList(null);
        // 2. 查询所有的一级分类
        ArrayList<JSONObject> resultList = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        Map<Long, List<BaseCategoryView>> category1Map = allCategoryViewList.stream()
                .collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        category1Map.forEach((category1Id, category1List) -> {
            // 通过一个json构造一个一级分类对象
            JSONObject category1Json = new JSONObject();
            category1Json.put("index", index.getAndSet(index.get() + 1));
            category1Json.put("categoryName", category1List.get(0).getCategory1Name());
            category1Json.put("categoryId", category1Id);
            // 3. 查询所有的二级分类
            ArrayList<JSONObject> category1Children = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> category2Map = category1List.stream()
                    .collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            category2Map.forEach((category2Id, category2List) -> {
                // 通过一个json构造一个二级分类对象
                JSONObject category2Json = new JSONObject();
                category2Json.put("categoryName", category2List.get(0).getCategory2Name());
                category2Json.put("categoryId", category2Id);
                // 4. 查询所有的三级分类
                ArrayList<JSONObject> category2Children = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> category3Map = category2List.stream()
                        .collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                category3Map.forEach((category3Id, category3List) -> {
                    // 通过一个json构造一个三级分类对象
                    JSONObject category3Json = new JSONObject();
                    category3Json.put("categoryName", category3List.get(0).getCategory3Name());
                    category3Json.put("categoryId", category3Id);
                    category2Children.add(category3Json);
                });
                category2Json.put("categoryChild", category2Children);
                // 把一个个二级分类放到category1Children里面
                category1Children.add(category2Json);
            });
            category1Json.put("categoryChild", category1Children);
            resultList.add(category1Json);
        });
        return resultList;
    }
}
