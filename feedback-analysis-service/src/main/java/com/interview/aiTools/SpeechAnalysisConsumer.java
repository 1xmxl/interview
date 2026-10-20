package com.interview.aiTools;

import com.interview.DTO.SpeechAnalysisMessage;
import com.interview.constant.RabbitMQConstant;
import com.interview.domain.po.SpeechAnalysisDoc;
import com.interview.service.SpeechAnalysisCalculator;
import com.interview.service.SpeechAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 语音分析消费端：消费 speech.analysis 消息，计算语速/填充词/流利度等指标并写入 ES。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpeechAnalysisConsumer {

    private final SpeechAnalysisCalculator calculator;
    private final SpeechAnalysisService speechAnalysisService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(RabbitMQConstant.Queue.SPEECH_ANALYSIS_QUEUE),
            exchange = @Exchange(RabbitMQConstant.Exchange.INTERVIEW_EXCHANGE),
            key = RabbitMQConstant.Key.SPEECH_ANALYSIS_KEY
    ))
    public void onSpeechAnalysis(SpeechAnalysisMessage message) {
        log.info("收到语音分析消息, sessionId={}, answerId={}", message.getSessionId(), message.getAnswerId());
        if (message.getAnswerId() == null) {
            log.warn("语音分析消息缺少 answerId，忽略");
            return;
        }
        try {
            // 幂等：answer_id 已存在则跳过
            if (speechAnalysisService.existsByAnswerId(message.getAnswerId())) {
                log.info("语音分析已存在，跳过, answerId={}", message.getAnswerId());
                return;
            }
            SpeechAnalysisDoc doc = calculator.calculate(message);
            speechAnalysisService.save(doc);
            log.info("语音分析结果已写入 ES, answerId={}, wordCount={}, fluency={}",
                    message.getAnswerId(), doc.getWordCount(), doc.getFluencyScore());
        } catch (Exception e) {
            log.error("语音分析失败, sessionId={}, answerId={}", message.getSessionId(), message.getAnswerId(), e);
            throw new RuntimeException(e);
        }
    }
}
