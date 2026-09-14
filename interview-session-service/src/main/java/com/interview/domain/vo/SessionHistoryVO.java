package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHistoryVO {
    private Long questionId;
    private String questionText;
    private String expectedSkills;
    private Integer orderIndex;
    private LocalDateTime askedAt;
    private Long answerId;
    private String answerText;
    private String audioFileUrl;
    private LocalDateTime submittedAt;
}