package com.interview.service;

import com.interview.domain.po.GrowthReports;
import com.interview.domain.vo.SkillHistoryVO;
import com.interview.domain.vo.SkillScoreVO;
import com.interview.domain.vo.WeaknessVO;

import java.util.List;

/**
 * 用户成长服务
 */
public interface IGrowthService {

    /** 当前用户各技能评分（每个技能取最近一次得分） */
    List<SkillScoreVO> getSkillScores(Long userId);

    /** 单技能历史趋势（period 形如 30d / 6m / 1y） */
    List<SkillHistoryVO> getSkillHistory(Long userId, String skill, String period);

    /** 成长报告列表（按生成时间倒序） */
    List<GrowthReports> listReports(Long userId);

    /** 报告详情 */
    GrowthReports getReport(Long userId, Long id);

    /** 薄弱点与提升建议（取最近得分低于 70 的技能，按分数升序取前 3） */
    List<WeaknessVO> getWeaknesses(Long userId);
}
