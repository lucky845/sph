package com.atguigu.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.result.RetVal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lucky845
 * @date 2022年04月13日
 */
@Controller
public class WebIndexController {

    @Resource
    private ProductFeignClient productFeignClient;

    /**
     * 查询首页分类信息
     */
    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        RetVal<List<JSONObject>> retVal = productFeignClient.getIndexCategory();
        model.addAttribute("list", retVal.getData());
        return "index/index";
    }

}
