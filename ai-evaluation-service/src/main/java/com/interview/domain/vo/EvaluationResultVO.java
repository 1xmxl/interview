package com.interview.domain.vo;

import com.interview.domain.po.AnswerEvaluations;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 完整评估结果 VO（evaluations/{sessionId} 返回）
 */
@Data
public class EvaluationResultVO {
    private Long sessionId;
    /** Markdown/HTML 格式完整报告 */
    private String reportContent;
    /** 维度分数 JSON 字符串，如 {"technical":85,"communication":72} */
    private String metrics;
    private LocalDateTime generatedAt;
    /** 各题评估明细 */
    private List<AnswerEvaluations> answers;
}
