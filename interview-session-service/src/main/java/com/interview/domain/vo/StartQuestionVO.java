package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开始面试返回：首个问题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartQuestionVO {
    private Long questionId;
    private String question;
    private Integer totalQuestions;
}
