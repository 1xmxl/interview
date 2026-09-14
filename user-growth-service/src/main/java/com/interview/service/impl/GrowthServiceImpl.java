package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.domain.po.GrowthReports;
import com.interview.domain.po.UserSkillProgress;
import com.interview.domain.vo.SkillHistoryVO;
import com.interview.domain.vo.SkillScoreVO;
import com.interview.domain.vo.WeaknessVO;
import com.interview.mapper.GrowthReportsMapper;
import com.interview.mapper.UserSkillProgressMapper;
import com.interview.service.IGrowthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户成长服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GrowthServiceImpl implements IGrowthService {

    /** 判定为薄弱点的分数阈值 */
    private static final BigDecimal WEAK_THRESHOLD = new BigDecimal("70");
    /** 薄弱点最多返回条数 */
    private static final int WEAK_LIMIT = 3;
    /** 默认趋势查询周期（6 个月） */
    private static final String DEFAULT_PERIOD = "6m";

    private final UserSkillProgressMapper progressMapper;
    private final GrowthReportsMapper reportsMapper;

    @Override
    public List<SkillScoreVO> getSkillScores(Long userId) {
        List<UserSkillProgress> all = queryLatestFirst(userId);
        // 按技能名去重，取最近一条（list 已按 recorded_at 倒序）
        Map<String, UserSkillProgress> latestBySkill = new LinkedHashMap<>();
        for (UserSkillProgress p : all) {
            latestBySkill.putIfAbsent(p.getSkillName(), p);
        }
        return latestBySkill.values().stream().map(p -> {
            SkillScoreVO vo = new SkillScoreVO();
            vo.setSkillName(p.getSkillName());
            vo.setScore(p.getScore());
            vo.setDimension(p.getDimension());
            vo.setRecordedAt(p.getRecordedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SkillHistoryVO> getSkillHistory(Long userId, String skill, String period) {
        LocalDateTime start = parsePeriod(period);
        List<UserSkillProgress> list = progressMapper.selectList(
                new LambdaQueryWrapper<UserSkillProgress>()
                        .eq(UserSkillProgress::getUserId, userId)
                        .eq(skill != null && !skill.isBlank(), UserSkillProgress::getSkillName, skill)
                        .ge(UserSkillProgress::getRecordedAt, start)
                        .orderByAsc(UserSkillProgress::getRecordedAt));
        return list.stream()
                .map(p -> new SkillHistoryVO(
                        p.getRecordedAt() == null ? null : p.getRecordedAt().toLocalDate().toString(),
                        p.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GrowthReports> listReports(Long userId) {
        return reportsMapper.selectList(
                new LambdaQueryWrapper<GrowthReports>()
                        .eq(GrowthReports::getUserId, userId)
                        .orderByDesc(GrowthReports::getGeneratedAt));
    }

    @Override
    public GrowthReports getReport(Long userId, Long id) {
        return reportsMapper.selectOne(
                new LambdaQueryWrapper<GrowthReports>()
                        .eq(GrowthReports::getId, id)
                        .eq(GrowthReports::getUserId, userId));
    }

    @Override
    public List<WeaknessVO> getWeaknesses(Long userId) {
        return queryLatestFirst(userId).stream()
                // 按技能名去重取最近一条
                .collect(Collectors.toMap(UserSkillProgress::getSkillName, p -> p, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .filter(p -> p.getScore() != null && p.getScore().compareTo(WEAK_THRESHOLD) < 0)
                .sorted(Comparator.comparing(UserSkillProgress::getScore))
                .limit(WEAK_LIMIT)
                .map(p -> {
                    WeaknessVO vo = new WeaknessVO();
                    vo.setSkillName(p.getSkillName());
                    vo.setScore(p.getScore());
                    vo.setDimension(p.getDimension());
                    vo.setSuggestion(suggestionFor(p.getDimension()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /** 查询当前用户全部技能记录，按时间倒序 */
    private List<UserSkillProgress> queryLatestFirst(Long userId) {
        return progressMapper.selectList(
                new LambdaQueryWrapper<UserSkillProgress>()
                        .eq(UserSkillProgress::getUserId, userId)
                        .orderByDesc(UserSkillProgress::getRecordedAt));
    }

    /** 解析周期参数：30d / 6m / 1y，非法或为空时默认 6m */
    private LocalDateTime parsePeriod(String period) {
        String p = (period == null || period.isBlank()) ? DEFAULT_PERIOD : period.trim().toLowerCase();
        try {
            char unit = p.charAt(p.length() - 1);
            int amount = Integer.parseInt(p.substring(0, p.length() - 1));
            return switch (unit) {
                case 'd' -> LocalDateTime.now().minusDays(amount);
                case 'm' -> LocalDateTime.now().minusMonths(amount);
                case 'y' -> LocalDateTime.now().minusYears(amount);
                default -> LocalDateTime.now().minusMonths(6);
            };
        } catch (Exception e) {
            log.warn("非法周期参数 period={}，使用默认 6m", period);
            return LocalDateTime.now().minusMonths(6);
        }
    }

    /** 按评估维度给出通用提升建议 */
    private String suggestionFor(String dimension) {
        if (dimension == null) {
            return "针对该技能做专项训练，定期复盘评估数据。";
        }
        return switch (dimension.toUpperCase()) {
            case "TECHNICAL" -> "加强技术深度：梳理高频考点与项目细节，每周完成 2 次针对性模拟面试。";
            case "COMMUNICATION" -> "提升表达结构化：使用 STAR 法则组织回答，录制回听减少填充词。";
            case "PROBLEM_SOLVING" -> "强化方案权衡：多练习系统设计题，补充方案对比与量化指标。";
            default -> "针对该技能做专项训练，定期复盘评估数据。";
        };
    }
}
