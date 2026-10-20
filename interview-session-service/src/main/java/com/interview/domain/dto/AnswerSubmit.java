package com.interview.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交答案请求体（文本或语音 URL，至少其一）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmit {
    private String text;
    private String audioUrl;
}
