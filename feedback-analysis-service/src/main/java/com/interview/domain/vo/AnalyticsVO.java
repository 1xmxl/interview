package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户语音能力聚合统计（与前端 feedback/analytics.vue 对齐）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsVO {
    private Double fluency;
    private Double pronunciation;
    private Double wpm;
    private List<FillerWordVO> fillerWords;
    private List<Integer> trend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FillerWordVO {
        private String word;
        private Integer count;
    }
}
