package com.interview.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能评分 VO（skills 接口返回）
 */
@Data
public class SkillScoreVO {
    private String skillName;
    private BigDecimal score;
    private String dimension;
    private LocalDateTime recordedAt;
}
