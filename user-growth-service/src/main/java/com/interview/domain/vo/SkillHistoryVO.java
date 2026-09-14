package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 技能历史趋势点（skills/history 接口返回）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillHistoryVO {
    private String date;
    private BigDecimal score;
}
