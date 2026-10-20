package com.interview.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 首页数据概览 VO（/users/me/stats）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO {
    private Long interviewCount;
    private BigDecimal avgScore;
    private Integer skillCount;
    private Integer reportCount;
}
