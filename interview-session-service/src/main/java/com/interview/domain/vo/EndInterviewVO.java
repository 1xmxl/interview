package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 结束面试推送体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndInterviewVO {
    private Long sessionId;
    private String status;
    private String message;
}
