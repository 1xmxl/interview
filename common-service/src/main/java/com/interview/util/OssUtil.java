package com.interview.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;


@Component
@Slf4j
public class OssUtil {
    @Value("${spring.aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${spring.aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${spring.aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${spring.aliyun.oss.bucket-name}")
    private String bucketName;
    private OSS ossClient;
    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
    public String uploadFile(MultipartFile file, String objectName) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            ossClient.putObject(putObjectRequest);
        }
        return objectName; // 返回 OSS 上的对象 key
    }

    public InputStream getObject(String objectName) {
        OSSObject ossObject = ossClient.getObject(bucketName, objectName);
        return ossObject.getObjectContent();
    }

    // 可选：生成带签名的临时访问链接（如需前端直接预览）
    public String generatePresignedUrl(String objectName, long expirationMillis) {
        Date expiration = new Date(System.currentTimeMillis() + expirationMillis);
        URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
        return url.toString();
    }
    public void deleteOssObject(String objectName) {
        ossClient.deleteObject(bucketName, objectName);
    }
}
