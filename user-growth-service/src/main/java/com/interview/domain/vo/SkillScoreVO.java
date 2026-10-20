package com.interview.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能评分 VO（skills 接口返回，字段与前端 mock 对齐：name/score/level）。
 */
@Data
public class SkillScoreVO {
    private String name;
    private String skillName;
    private BigDecimal score;
    private String level;
    private String dimension;
    private LocalDateTime recordedAt;
}
