package com.interview.aiTools;

import com.interview.DTO.ReportGenerateMessage;
import com.interview.constant.RabbitMQConstant;
import com.interview.service.IEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 报告生成消费端：消费 report.generate 消息，汇总评估结果生成综合报告并回写。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerateConsumer {

    private final IEvaluationService evaluationService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(RabbitMQConstant.Queue.REPORT_GENERATE_QUEUE),
            exchange = @Exchange(RabbitMQConstant.Exchange.INTERVIEW_EXCHANGE),
            key = RabbitMQConstant.Key.REPORT_GENERATE_KEY
    ))
    public void onReportGenerate(ReportGenerateMessage message) {
        log.info("收到报告生成消息, sessionId={}", message.getSessionId());
        try {
            evaluationService.generateReport(message.getSessionId(), message.getUserId());
        } catch (Exception e) {
            log.error("报告生成失败, sessionId={}", message.getSessionId(), e);
            throw new RuntimeException(e);
        }
    }
}
