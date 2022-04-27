package com.atguigu.controller;

import com.atguigu.client.SeckillFeignClient;
import com.atguigu.entity.SeckillProduct;
import com.atguigu.result.RetVal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author lucky845
 * @since 2022年04月26日
 */
@Controller
public class WebSecKillController {

    @Resource
    private SeckillFeignClient seckillFeignClient;

    /**
     * 秒杀首页
     */
    @GetMapping("/seckill-index.html")
    public String seckillIndex(Model model) {
        RetVal<List<Object>> retVal = seckillFeignClient.queryAllSeckillProduct();
        model.addAttribute("list", retVal.getData());
        return "seckill/index";
    }

    /**
     * 秒杀商品详情页面
     *
     * @param skuId 秒杀商品skuId
     */
    @GetMapping("/seckill-detail/{skuId}.html")
    public String seckillDetail(@PathVariable Long skuId, Model model) {
        RetVal<SeckillProduct> retVal = seckillFeignClient.getSeckillProductBySkuId(skuId);
        model.addAttribute("item", retVal.getData());
        return "seckill/detail";
    }

    /**
     * 获取下单码成功之后要访问的页面
     *
     * @param skuId       商品skuId
     * @param seckillCode 秒杀码
     */
    @GetMapping("/seckill-queue.html")
    public String seckillQueue(Long skuId, String seckillCode, HttpServletRequest request) {
        request.setAttribute("skuId", skuId);
        request.setAttribute("seckillCode", seckillCode);
        return "seckill/queue";
    }

    /**
     * 确认下单信息
     */
    @GetMapping("/seckill-confirm.html")
    public String seckillConfirm(Model model, HttpServletRequest request) {
        //在秒杀系统里面查询信息给页面提供数据
        RetVal retVal = seckillFeignClient.seckillConfirm();
        if (retVal.isOk()) {
            Map<String, Object> retMap = (Map<String, Object>) retVal.getData();
            model.addAllAttributes(retMap);
        } else {
            model.addAttribute("message", retVal.getMessage());
        }
        return "seckill/confirm";
    }

}
