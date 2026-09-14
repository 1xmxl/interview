package com.interview.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class AiClientConfig {

    @Value("${spring.ai.dashscope.api-key:}")
    private String dashScopeApiKey;

    @Bean
    public RestClient.Builder dashScopeRestClientBuilder() {
        // 1. 创建一个带有超时设置的 JDK HttpClient (Java 11+)
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // 连接超时
                .build();

        // 2. 将其包装为 Spring 的 ClientHttpRequestFactory
        ClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        // 3. 创建并返回一个配置好的 RestClient.Builder
        return RestClient.builder()
                .requestFactory(requestFactory);
    }

    @Bean
    public DashScopeChatModel chatModel(RestClient.Builder dashScopeRestClientBuilder) {
        // 4. 使用这个 builder 来构建 DashScopeApi
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .restClientBuilder(dashScopeRestClientBuilder) // 注入自定义的builder
                .build();

        // 5. 配置模型选项
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel("qwen-plus")
                .build();

        // 6. 构建模型
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
    }
}