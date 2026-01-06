package com.fba.logi.infrastructure.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置
 */
@Slf4j
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private int port;

    @Value("${milvus.database:default}")
    private String database;

    @Value("${milvus.enabled:true}")
    private boolean enabled;

    @Bean
    public MilvusServiceClient milvusClient() {
        if (!enabled) {
            log.warn("Milvus 已禁用，将返回空客户端");
            return null;
        }

        try {
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(host)
                    .withPort(port)
                    .withDatabaseName(database)
                    .build();

            MilvusServiceClient client = new MilvusServiceClient(connectParam);
            log.info("Milvus 客户端连接成功，地址: {}:{}", host, port);
            return client;
        } catch (Exception e) {
            log.error("Milvus 客户端连接失败: {}", e.getMessage());
            throw new RuntimeException("Milvus 连接失败", e);
        }
    }

}
