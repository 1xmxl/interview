package com.interview.config;

import com.interview.service.SpeechAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时确保 speech_analytics 索引存在（ES 不可用时仅告警，不阻塞启动）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final RestClient restClient;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Request head = new Request("HEAD", "/" + SpeechAnalysisService.SPEECH_ANALYTICS_INDEX);
            Response response = restClient.performRequest(head);
            int status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                Request create = new Request("PUT", "/" + SpeechAnalysisService.SPEECH_ANALYTICS_INDEX);
                Response created = restClient.performRequest(create);
                if (created.getStatusLine().getStatusCode() < 300) {
                    log.info("已创建 ES 索引: {}", SpeechAnalysisService.SPEECH_ANALYTICS_INDEX);
                }
            } else if (status == 200) {
                log.info("ES 索引已存在: {}", SpeechAnalysisService.SPEECH_ANALYTICS_INDEX);
            }
        } catch (Exception e) {
            log.warn("初始化 ES 索引失败（不影响启动）: {}", e.getMessage());
        }
    }
}
