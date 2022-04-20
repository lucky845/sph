package com.atguigu.controller;

import com.atguigu.client.OrderFeignClient;
import com.atguigu.result.RetVal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author lucky845
 * @since 2022年04月20日
 */
@Controller
public class WebOrderController {

    @Resource
    private OrderFeignClient orderFeignClient;

    /**
     * 跳转到订单确认页面
     */
    @GetMapping("/confirm.html")
    public String confirm(Model model) {
        RetVal<Map<String, Object>> retVal = orderFeignClient.confirm();
        model.addAllAttributes(retVal.getData());
        return "order/confirm";
    }


}
