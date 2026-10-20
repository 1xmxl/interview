package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试结束后的报告生成消息体（interview-session-service 发 → ai-evaluation-service 消费）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerateMessage {
    private Long sessionId;
    private Long userId;
}
