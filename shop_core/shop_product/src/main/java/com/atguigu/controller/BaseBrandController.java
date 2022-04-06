package com.atguigu.controller;


import com.atguigu.entity.BaseBrand;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseBrandService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 品牌表 前端控制器
 * </p>
 *
 * @author lucky845
 * @since 2022-04-06
 */
@Api(tags = "品牌信息")
@RestController
@RequestMapping("/product/brand")
public class BaseBrandController {

    @Resource
    private BaseBrandService baseBrandService;

    @Value("${fastdfs.fix}")
    private String fastdfsfix;

    /**
     * 查询品牌信息分页记录
     *
     * @param currentPageNum 当前页码
     * @param pageSize       每页记录数
     */
    @ApiOperation("查询品牌信息分页记录")
    @GetMapping("/queryBrandByPage/{currentPageNum}/{pageSize}")
    public RetVal<Page<BaseBrand>> queryBrandByPage(
            @ApiParam(name = "currentPageNum", value = "当前页码", required = true)
            @PathVariable Long currentPageNum,

            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @PathVariable Long pageSize
    ) {
        Page<BaseBrand> page = new Page<>(currentPageNum, pageSize);
        baseBrandService.page(page, null);
        return RetVal.ok(page);
    }

    /**
     * 添加品牌信息
     *
     * @param baseBrand 品牌信息对象
     */
    @ApiOperation("添加品牌信息")
    @PostMapping
    private RetVal<Object> saveBrand(
            @ApiParam(name = "baseBrand", value = "品牌信息对象", required = true)
            @RequestBody BaseBrand baseBrand
    ) {
        baseBrandService.save(baseBrand);
        return RetVal.ok();
    }

    /**
     * 根据品牌id查询品牌信息
     *
     * @param brandId 品牌id
     */
    @ApiOperation("根据品牌id查询品牌信息")
    @GetMapping("/{brandId}")
    public RetVal<BaseBrand> getBrandById(
            @ApiParam(name = "brandId", value = "品牌id", required = true)
            @PathVariable Long brandId
    ) {
        BaseBrand brand = baseBrandService.getById(brandId);
        return RetVal.ok(brand);
    }


    /**
     * 更新品牌信息
     *
     * @param baseBrand 品牌信息对象
     */
    @ApiOperation("更新品牌信息")
    @PutMapping
    public RetVal<Object> updateBrand(
            @ApiParam(name = "baseBrand", value = "品牌信息对象", required = true)
            @RequestBody BaseBrand baseBrand
    ) {
        baseBrandService.updateById(baseBrand);
        return RetVal.ok();
    }

    /**
     * 根据brandId删除品牌信息
     *
     * @param brandId 品牌id
     */
    @ApiOperation("根据brandId删除品牌信息")
    @DeleteMapping("/{brandId}")
    public RetVal<Object> deleteBrand(
            @ApiParam(name = "brandId", value = "品牌id", required = true)
            @PathVariable Long brandId
    ) {
        baseBrandService.removeById(brandId);
        return RetVal.ok();
    }

    /**
     * 查询所有的品牌
     */
    @ApiOperation("查询所有的品牌")
    @GetMapping("/getAllBrand")
    public RetVal<List<BaseBrand>> getAllBrand() {
        List<BaseBrand> baseBrandList = baseBrandService.list(null);
        return RetVal.ok(baseBrandList);
    }

    /**
     * 利用fastdfs进行文件上传
     *
     * @param file 需要上传的文件
     */
    @ApiOperation("利用fastdfs进行文件上传")
    @PostMapping("/fileUpload")
    public RetVal<String> fileUpload(
            @ApiParam(name = "file", value = "需要上传的文件", required = true)
                    MultipartFile file
    ) throws Exception {
        // 需要一个配制文件告诉fastdfs在哪里
        String configFilePath = Objects.requireNonNull(this.getClass().getResource("/tracker.conf")).getFile();
        // 初始化
        ClientGlobal.init(configFilePath);
        // 创建trackerClient客户端
        TrackerClient trackerClient = new TrackerClient();
        // 用trackerClient获取连接
        TrackerServer trackerServer = trackerClient.getConnection();
        // 创建StorageClient1
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
        // 对文件实现上传
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String path = storageClient1.upload_file1(file.getBytes(), extension, null);
        System.out.println("文件访问地址： " + fastdfsfix + path);
        return RetVal.ok(fastdfsfix + path);
    }

}

