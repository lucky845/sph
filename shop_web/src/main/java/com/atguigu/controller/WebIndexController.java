package com.atguigu.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.client.SearchFeignClient;
import com.atguigu.result.RetVal;
import com.atguigu.search.SearchParam;
import com.atguigu.search.SearchResponseVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lucky845
 * @date 2022年04月13日
 */
@Controller
public class WebIndexController {

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private SearchFeignClient searchFeignClient;

    /**
     * 查询首页分类信息
     */
    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        RetVal<List<JSONObject>> retVal = productFeignClient.getIndexCategory();
        model.addAttribute("list", retVal.getData());
        return "index/index";
    }

    /**
     * 跳转到搜索页面，展示搜索结果
     *
     * @param searchParam 搜索条件对象
     */
    @GetMapping("/search.html")
    public String searchProduct(SearchParam searchParam, Model model) {
        // 远程调用search微服务实现搜索功能
        SearchResponseVo searchResponseVo = searchFeignClient.searchProduct(searchParam).getData();
        model.addAllAttributes(JSONObject.parseObject(JSONObject.toJSONString(searchResponseVo), Map.class));
        // 1. 浏览器url路径后面参数的回显
        String urlParam = pageUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);
        // 2. 页面的品牌信息回显
        String brandNameParam = pageBrandParam(searchParam.getBrandName());
        model.addAttribute("brandNameParam", brandNameParam);
        // 3. 页面的平台属性回显
        List<Map<String, String>> propsParamList = pagePlatformProperty(searchParam.getProps());
        model.addAttribute("propsParamList", propsParamList);
        // 4. 页面的排序信息回显
        Map<String, Object> orderMap = pageSortParam(searchParam.getOrder());
        model.addAttribute("orderMap", orderMap);
        return "search/index";
    }

    /**
     * 商品排序信息回显
     *
     * @param order 商品排序信息
     */
    private Map<String, Object> pageSortParam(String order) {
        Map<String, Object> orderMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
            String[] orderSplit = order.split(":");
            if (orderSplit.length == 2) {
                orderMap.put("type", orderSplit[0]);
                orderMap.put("sort", orderSplit[1]);
            }
        } else {
            // 默认给一个排序
            orderMap.put("type", 1);
            orderMap.put("sort", "desc");
        }
        return orderMap;
    }

    /**
     * 商品的平台属性回显
     *
     * @param props 商品的平台属性数组
     */
    private List<Map<String, String>> pagePlatformProperty(String[] props) {
        ArrayList<Map<String, String>> propList = new ArrayList<>();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] propSplit = prop.split(":");
                if (propSplit.length == 3) {
                    HashMap<String, String> propMap = new HashMap<>();
                    propMap.put("propertyKeyId", propSplit[0]);
                    propMap.put("propertyKey", propSplit[2]);
                    propMap.put("propertyValue", propSplit[1]);
                    propList.add(propMap);
                }
            }
        }
        return propList;
    }

    /**
     * 商品的品牌信息回显
     *
     * @param brandName 商品名称
     */
    private String pageBrandParam(String brandName) {
        if (!StringUtils.isEmpty(brandName)) {
            String[] brandNameSplit = brandName.split(":");
            if (brandNameSplit.length == 2) {
                return "品牌：" + brandNameSplit[1];
            }
        }
        return null;
    }

    /**
     * 浏览器url路径后面参数的回显
     *
     * @param searchParam 搜索条件对象
     */
    private String pageUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();
        // 判断是否有关键字
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        // 判断是否有商品分类id
        Long category1Id = searchParam.getCategory1Id();
        if (category1Id != null) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        Long category2Id = searchParam.getCategory2Id();
        if (category2Id != null) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        Long category3Id = searchParam.getCategory3Id();
        if (category3Id != null) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        // 判断是否有品牌名称
        String brandName = searchParam.getBrandName();
        if (!StringUtils.isEmpty(brandName)) {
            if (urlParam.length() > 0) {
                // 原有地址栏有参数才继续往下拼接
                urlParam.append("&brandName=").append(searchParam.getBrandName());
            }
        }
        // 判断是否有平台属性
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            if (urlParam.length() > 0) {
                // 源地址栏有参数才继续往下拼接
                for (String prop : props) {
                    urlParam.append("&props=").append(prop);
                }
            }
        }
        return "search.html?" + urlParam;
    }

}
