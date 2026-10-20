package com.interview.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI 综合报告结果（LLM 结构化返回）
 */
@Data
public class ReportResult {
    private BigDecimal overallScore;
    private String summary;
    private String strengths;
    private String weaknesses;
    /** 各维度得分，如 {"technical":85,"communication":72} */
    private Map<String, BigDecimal> metrics;
}
