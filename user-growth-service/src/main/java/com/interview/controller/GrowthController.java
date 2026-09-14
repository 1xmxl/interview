package com.interview.controller;

import com.interview.context.UserContext;
import com.interview.domain.po.GrowthReports;
import com.interview.domain.vo.SkillHistoryVO;
import com.interview.domain.vo.SkillScoreVO;
import com.interview.domain.vo.WeaknessVO;
import com.interview.service.IGrowthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 用户成长控制器
 * </p>
 */
@RestController
@RequestMapping("/growth")
@RequiredArgsConstructor
@Slf4j
public class GrowthController {

    private final IGrowthService growthService;

    /** 当前用户各维度技能评分 */
    @GetMapping("/skills")
    public List<SkillScoreVO> getSkills() {
        Long userId = currentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        log.info("获取用户技能评分, userId={}", userId);
        return growthService.getSkillScores(userId);
    }

    /** 单技能历史趋势，如 /growth/skills/history?skill=Java&period=6m */
    @GetMapping("/skills/history")
    public List<SkillHistoryVO> getSkillHistory(@RequestParam(required = false) String skill,
                                                @RequestParam(required = false) String period) {
        Long userId = currentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        log.info("获取技能历史趋势, userId={}, skill={}, period={}", userId, skill, period);
        return growthService.getSkillHistory(userId, skill, period);
    }

    /** 成长报告列表 */
    @GetMapping("/reports")
    public List<GrowthReports> listReports() {
        Long userId = currentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        log.info("获取成长报告列表, userId={}", userId);
        return growthService.listReports(userId);
    }

    /** 报告详情 */
    @GetMapping("/reports/{id}")
    public GrowthReports getReport(@PathVariable Long id) {
        Long userId = currentUserId();
        if (userId == null) {
            return null;
        }
        log.info("获取成长报告详情, userId={}, id={}", userId, id);
        return growthService.getReport(userId, id);
    }

    /** 当前薄弱点与提升建议 */
    @GetMapping("/weaknesses")
    public List<WeaknessVO> getWeaknesses() {
        Long userId = currentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        log.info("获取薄弱点列表, userId={}", userId);
        return growthService.getWeaknesses(userId);
    }

    /** 从请求上下文解析当前用户 ID（未认证返回 null） */
    private Long currentUserId() {
        try {
            String uid = UserContext.getUserId();
            return uid == null || uid.isBlank() ? null : Long.parseLong(uid);
        } catch (Exception e) {
            log.warn("解析当前用户 ID 失败: {}", e.getMessage());
            return null;
        }
    }
}
