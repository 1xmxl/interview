package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成长统计（user-growth-service 提供，供首页数据概览聚合）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthStatsDTO {
    private Integer skillCount;
    private Integer reportCount;
}
