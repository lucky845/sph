package com.atguigu.utils;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author lucky845
 * @date 2022年04月06日 10:46
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioUpload {

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private MinioClient minioClient;

    @Bean
    public MinioClient getMinioClient() throws Exception {
        // 使用Minio服务的URL，Access key和Secret key创建一个MinioClient对象
        MinioClient minioClient = new MinioClient(
                minioProperties.getEndpoint(),
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey()
        );
        // 检查桶对象是否已经存在
        boolean isExist = minioClient.bucketExists(minioProperties.getBucketName());
        if (!isExist) {
            // 创建一个名为lucky845的存储桶
            minioClient.makeBucket(minioProperties.getBucketName());
        }
        return minioClient;
    }

    /**
     * 使用minio上传文件
     *
     * @param file 需要上传的文件
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 获取文件名称
        String filename = UUID.randomUUID().toString().replaceAll("-", "")
                + file.getOriginalFilename();
        // 使用putObject上传一个文件到存储桶中
        InputStream inputStream = file.getInputStream();
        PutObjectOptions putObjectOptions = new PutObjectOptions(inputStream.available(), -1);
        minioClient.putObject(minioProperties.getBucketName(), filename, inputStream, putObjectOptions);
        // 拼接文件地址
        String retUrl = minioProperties.getEndpoint() + File.separator
                + minioProperties.getBucketName() + File.separator + filename;
        return retUrl;
    }

}
