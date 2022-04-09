package com.atguigu.controller;

import com.atguigu.service.ConcurrentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试Redis作为分布式锁
 *
 * @author lucky845
 * @date 2022年04月09日
 */
@Api(tags = "测试Redis作为分布式锁")
@RequestMapping("/product")
@RestController
public class ConcurrentController {

    @Resource
    private ConcurrentService concurrentService;

    /**
     * 测试分布式锁
     */
    @ApiOperation("测试方法")
    @GetMapping("/setNum")
    public String setNum() {
        concurrentService.setNum();
        return "success";
    }


}
