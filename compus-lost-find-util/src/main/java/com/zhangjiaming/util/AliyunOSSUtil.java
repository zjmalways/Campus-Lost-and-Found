package com.zhangjiaming.util;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.zhangjiaming.context.ErrorContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云 OSS 对象存储工具类。
 * 凭证支持两种方式（yml 优先）：
 *   1. application.yml: aliyun.oss.access-key-id / access-key-secret
 *   2. 环境变量: ALIBABA_CLOUD_ACCESS_KEY_ID / ALIBABA_CLOUD_ACCESS_KEY_SECRET
 * 未配置时服务照常启动，仅头像/图片上传接口返回友好提示。
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSUtil {

    @Setter @Getter private String endpoint;
    @Setter @Getter private String bucketName;
    @Setter @Getter private String region;
    @Setter @Getter private String avatarDir = "avatar";
    @Setter @Getter private String imagesDir = "images";
    @Setter @Getter private String accessKeyId;
    @Setter @Getter private String accessKeySecret;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        try {
            CredentialsProvider provider;
            if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret)) {
                provider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            } else {
                provider = new EnvironmentVariableCredentialsProvider();
            }

            ClientBuilderConfiguration config = new ClientBuilderConfiguration();
            config.setSignatureVersion(SignVersion.V4);

            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(provider)
                    .clientConfiguration(config)
                    .region(region)
                    .build();

            log.info("阿里云 OSS 客户端初始化成功 —— bucket: {}, region: {}", bucketName, region);
        } catch (Exception e) {
            log.warn("阿里云 OSS 客户端初始化失败（头像上传功能不可用）: {}", e.getMessage());
            ossClient = null;
        }
    }

    private void ensureClient() {
        if (ossClient == null) {
            throw new RuntimeException(ErrorContext.OSS_NOT_CONFIGURED);
        }
    }

    /**
     * 上传文件到指定目录，返回可公开访问的 URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException(ErrorContext.FILE_EMPTY);
        }
        ensureClient();

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = directory + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream));
            return endpoint.replace("https://", "https://" + bucketName + ".") + "/" + objectName;
        } catch (IOException e) {
            log.error("OSS 上传失败: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorContext.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * 上传头像（存放到 avatar 目录）
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, avatarDir);
    }

    /**
     * 上传物品图片（存放到 images 目录）
     */
    public String uploadItemImage(MultipartFile file) {
        return uploadFile(file, imagesDir);
    }

    /**
     * 批量上传物品图片
     */
    public List<String> uploadItemImages(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.length == 0) {
            return urls;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                urls.add(uploadItemImage(file));
            }
        }
        return urls;
    }

    /**
     * 删除 OSS 上的文件
     */
    public void deleteFile(String objectUrl) {
        if (objectUrl == null || objectUrl.isEmpty()) {
            return;
        }
        ensureClient();
        String baseUrl = endpoint.replace("https://", "https://" + bucketName + ".");
        if (!objectUrl.startsWith(baseUrl)) {
            log.warn("非本 OSS 文件，跳过删除: {}", objectUrl);
            return;
        }
        String objectName = objectUrl.substring(baseUrl.length() + 1);
        ossClient.deleteObject(bucketName, objectName);
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("阿里云 OSS 客户端已关闭");
        }
    }
}
