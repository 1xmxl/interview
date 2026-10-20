package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语音分析消息体（interview-session-service 发 → feedback-analysis-service 消费）。
 * 提交答案后发送，携带转写文本与音频地址，消费端计算语速/停顿/填充词等指标并写入 ES。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechAnalysisMessage {
    private Long sessionId;
    private Long questionId;
    private Long answerId;
    private Long userId;
    private String audioUrl;
    private String answerText;
}
