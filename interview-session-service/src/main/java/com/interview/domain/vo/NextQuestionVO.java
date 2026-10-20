package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交答案返回：下一题（或 hasNext=false 表示面试结束）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextQuestionVO {
    private Boolean hasNext;
    private Long questionId;
    private String nextQuestion;
}
