package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开始面试返回：AI 面试官的开场白（含第一个问题）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartQuestionVO {
    private Long questionId;
    /** AI 开场白 + 第一问（自然口语，直接展示给用户） */
    private String question;
    /** 已废弃：不再按固定题数限制。保留字段兼容旧前端，值恒为 0 */
    private Integer totalQuestions;
}