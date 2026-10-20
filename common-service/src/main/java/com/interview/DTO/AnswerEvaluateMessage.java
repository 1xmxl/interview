package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交回答后的评估消息体（interview-session-service 发 → ai-evaluation-service 消费）
 * 直接携带题目与答案文本，消费端无需回查会话服务。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEvaluateMessage {
    private Long sessionId;
    private Long questionId;
    private Long answerId;
    private Long userId;
    private String questionText;
    private String answerText;
}
