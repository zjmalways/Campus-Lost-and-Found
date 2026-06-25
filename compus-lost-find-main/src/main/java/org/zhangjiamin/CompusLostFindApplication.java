package org.zhangjiamin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.zhangjiamin.util.AliyunOSSUtil;

@SpringBootApplication
@EnableConfigurationProperties(AliyunOSSUtil.class)
public class CompusLostFindApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompusLostFindApplication.class, args);
    }



}
