package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 面试统计（interview-session-service 提供，供首页数据概览聚合）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatsDTO {
    private Long interviewCount;
    private BigDecimal avgScore;
}
