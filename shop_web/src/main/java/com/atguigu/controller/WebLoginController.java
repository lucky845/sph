package com.atguigu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lucky845
 * @date 2022年04月18日
 */
@Controller
public class WebLoginController {

    /**
     * 跳转到登陆页面
     */
    @GetMapping("/login.html")
    public String login(HttpServletRequest request) {
        String originalUrl = request.getParameter("originalUrl");
        // 返回给前端，用于登陆成功后跳转回原来的页面
        request.setAttribute("originalUrl", originalUrl);
        // 返回登陆页面
        return "login";
    }

}
