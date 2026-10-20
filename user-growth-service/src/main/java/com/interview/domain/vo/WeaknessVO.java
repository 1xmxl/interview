package com.interview.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薄弱点 VO（weaknesses 接口返回，字段与前端 mock 对齐：skill/severity/suggestion）。
 */
@Data
public class WeaknessVO {
    private String skill;
    private String skillName;
    private BigDecimal score;
    private String severity;
    private String dimension;
    private String suggestion;
}
