package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 评估结果回写 DTO（ai-evaluation-service 通过 Feign 回写 interview-session-service）
 * 对应 session_db 的 interview_results 表 + interview_sessions.status。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResultDTO {
    private BigDecimal overallScore;
    private String summary;
    private String strengths;
    private String weaknesses;
}
