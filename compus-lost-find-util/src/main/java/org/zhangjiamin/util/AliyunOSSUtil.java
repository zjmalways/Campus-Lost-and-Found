package org.zhangjiamin.util;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 对象存储工具类
 * 使用前需设置环境变量：
 *   OSS_ACCESS_KEY_ID
 *   OSS_ACCESS_KEY_SECRET
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSUtil {

    @Setter @Getter
    private String endpoint;

    @Setter @Getter
    private String bucketName;

    @Setter @Getter
    private String region;

    /** 头像文件存储的目录前缀 */
    @Setter @Getter
    private String avatarDir = "avatar";

    private OSS ossClient;

    /**
     * 初始化 OSS 客户端（Bean 初始化后自动调用）
     */
    @PostConstruct
    public void init() {
        try {
            // 从环境变量获取访问凭证
            EnvironmentVariableCredentialsProvider credentialsProvider =
                    CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

            ClientBuilderConfiguration clientConfig = new ClientBuilderConfiguration();
            clientConfig.setSignatureVersion(SignVersion.V4);

            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(clientConfig)
                    .region(region)
                    .build();

            log.info("阿里云 OSS 客户端初始化成功 —— Bucket: {}, Region: {}", bucketName, region);
        } catch (Exception e) {
            log.error("阿里云 OSS 客户端初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("OSS 初始化失败，请检查环境变量 OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET", e);
        }
    }

    /**
     * 上传文件到 OSS
     *
     * @param file      上传的文件（MultipartFile）
     * @param directory 存储目录（如 "avatar"、"images"），自动拼接在对象名前
     * @return 可公开访问的文件 URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 生成唯一文件名，防止覆盖
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = directory + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            log.info("OSS 上传成功 —— 对象名: {}, ETag: {}", objectName, result.getETag());

            // 返回可公开访问的 URL
            return endpoint.replace("https://", "https://" + bucketName + ".")
                    + "/" + objectName;
        } catch (IOException e) {
            log.error("OSS 上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传头像文件（自动存放到 avatar 目录）
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, avatarDir);
    }

    /**
     * 删除 OSS 上的文件
     *
     * @param objectUrl 文件的完整 URL
     */
    public void deleteFile(String objectUrl) {
        if (objectUrl == null || objectUrl.isEmpty()) {
            return;
        }

        // 从 URL 中提取对象名
        String baseUrl = endpoint.replace("https://", "https://" + bucketName + ".");
        if (!objectUrl.startsWith(baseUrl)) {
            log.warn("非本 OSS 文件，跳过删除: {}", objectUrl);
            return;
        }

        String objectName = objectUrl.substring(baseUrl.length() + 1);
        ossClient.deleteObject(bucketName, objectName);
        log.info("OSS 删除成功 —— 对象名: {}", objectName);
    }

    /**
     * 销毁 OSS 客户端（Bean 销毁时自动调用）
     */
    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("阿里云 OSS 客户端已关闭");
        }
    }
}
