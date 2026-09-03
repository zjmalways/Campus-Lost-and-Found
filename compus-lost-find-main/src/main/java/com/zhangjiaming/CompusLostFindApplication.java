package com.zhangjiaming;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhangjiaming.mapper")
public class CompusLostFindApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompusLostFindApplication.class, args);
    }
}
