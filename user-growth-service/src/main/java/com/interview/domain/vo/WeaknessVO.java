package com.interview.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薄弱点 VO（weaknesses 接口返回）
 */
@Data
public class WeaknessVO {
    private String skillName;
    private BigDecimal score;
    private String dimension;
    private String suggestion;
}
