package com.interview.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 单题评估结果（LLM 结构化返回）
 */
@Data
public class AnswerEvaluationResult {
    private BigDecimal technicalScore;
    private BigDecimal clarityScore;
    private BigDecimal depthScore;
    private String feedbackText;
    private String improvementSuggestions;
}
