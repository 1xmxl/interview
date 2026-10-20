package com.interview.aiTools;

import com.interview.DTO.AnswerEvaluateMessage;
import com.interview.constant.RabbitMQConstant;
import com.interview.domain.po.AnswerEvaluations;
import com.interview.domain.vo.AnswerEvaluationResult;
import com.interview.mapper.AnswerEvaluationsMapper;
import com.interview.service.EvaluationAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 答案评估消费端：消费 answer.evaluate 消息，调用 AI 评估并入库。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnswerEvaluateConsumer {

    private final EvaluationAIService evaluationAIService;
    private final AnswerEvaluationsMapper answerEvaluationsMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(RabbitMQConstant.Queue.ANSWER_EVALUATE_QUEUE),
            exchange = @Exchange(RabbitMQConstant.Exchange.INTERVIEW_EXCHANGE),
            key = RabbitMQConstant.Key.ANSWER_EVALUATE_KEY
    ))
    public void onAnswerEvaluate(AnswerEvaluateMessage message) {
        log.info("收到答案评估消息, sessionId={}, answerId={}", message.getSessionId(), message.getAnswerId());
        try {
            AnswerEvaluationResult result = evaluationAIService.evaluateAnswer(
                    message.getQuestionText(), message.getAnswerText());

            AnswerEvaluations po = new AnswerEvaluations();
            po.setSessionId(message.getSessionId());
            po.setQuestionId(message.getQuestionId());
            po.setAnswerId(message.getAnswerId());
            po.setTechnicalScore(result.getTechnicalScore());
            po.setClarityScore(result.getClarityScore());
            po.setDepthScore(result.getDepthScore());
            po.setFeedbackText(result.getFeedbackText());
            po.setImprovementSuggestions(result.getImprovementSuggestions());
            po.setEvaluatedAt(LocalDateTime.now());
            answerEvaluationsMapper.insert(po);
            log.info("答案评估入库完成, answerId={}", message.getAnswerId());
        } catch (Exception e) {
            log.error("答案评估失败, sessionId={}, answerId={}", message.getSessionId(), message.getAnswerId(), e);
            throw new RuntimeException(e);
        }
    }
}
