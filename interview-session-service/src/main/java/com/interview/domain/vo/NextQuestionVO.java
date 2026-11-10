package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交答案返回：AI 面试官的下一轮回复。
 *
 * 与旧版的区别：`aiMessage` 是一整段自然对话（先回应上一答，再提问或收尾），
 * `hasNext=false` 表示 AI 判断面试已结束，此时 `aiMessage` 是结束语。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextQuestionVO {
    /** 是否还有后续（false = AI 认为面试结束） */
    private Boolean hasNext;
    /** 本轮 AI 提问对应的题目 ID（结束时为 null） */
    private Long questionId;
    /** AI 的完整回复（含回应 + 提问/追问，或结束语），直接展示给用户 */
    private String aiMessage;
    /** 兼容旧前端：等同 aiMessage */
    private String nextQuestion;
    /** 已问过的题目数 */
    private Integer askedCount;
}