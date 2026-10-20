package com.interview.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 低层 REST Client 配置（兼容 ES 7.x）。
 */
@Configuration
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestClient restClient(@Value("${spring.elasticsearch.uris}") String uris) {
        return RestClient.builder(HttpHost.create(uris)).build();
    }
}
