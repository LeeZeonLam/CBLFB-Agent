package com.fba.logi.writing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 多模态写作分析服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.fba.logi")
@EnableDiscoveryClient
public class WritingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WritingServiceApplication.class, args);
    }

}
