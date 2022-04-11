package com.atguigu.controller;

import com.atguigu.entity.SkuInfo;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RBloomFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 测试布隆过滤器
 *
 * @author lucky845
 * @date 2022年04月11日
 */
@RestController
@RequestMapping("/init")
public class BloomController {

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private RBloomFilter<Object> bloomFilter;

    // TODO 后期加入定时任务后，对布隆过滤器进行维护
    @GetMapping("/sku/bloom")
    public String SkuBloom() {
        // 先删除之前的数据
        bloomFilter.delete();
        // 重新初始化数据
        bloomFilter.tryInit(100000000L, 0.0001);
        // 查询数据库所有的SkuInfo信息
        QueryWrapper<SkuInfo> wrapper = new QueryWrapper<>();
        // 只把id字段查出来
        wrapper.select("id");
        List<SkuInfo> skuInfoList = skuInfoService.list(wrapper);
        // 把所有的SkuInfo放入布隆过滤器
        for (SkuInfo skuInfo : skuInfoList) {
            bloomFilter.add(skuInfo.getId());
        }
        return "布隆过滤器初始化成功";
    }

}
