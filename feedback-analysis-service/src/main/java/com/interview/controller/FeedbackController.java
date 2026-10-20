package com.interview.controller;

import com.interview.context.UserContext;
import com.interview.domain.vo.AnalyticsVO;
import com.interview.domain.vo.SpeechFeatureVO;
import com.interview.service.SpeechAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 反馈分析控制器（语音分析详情 + 聚合统计）。
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final SpeechAnalysisService speechAnalysisService;

    /** 获取语音分析详情（从 ES 读取） */
    @GetMapping("/{sessionId}/speech")
    public List<SpeechFeatureVO> getSpeech(@PathVariable("sessionId") Long sessionId) {
        Long userId = currentUserId();
        log.info("获取语音分析详情, sessionId={}, userId={}", sessionId, userId);
        return speechAnalysisService.getSpeechDetail(sessionId, userId);
    }

    /** 用户语音能力聚合统计，如 /feedback/analytics?userId=1&period=30d */
    @GetMapping("/analytics")
    public AnalyticsVO getAnalytics(@RequestParam(name = "userId", required = false) Long userId,
                                    @RequestParam(name = "period", defaultValue = "30d") String period) {
        Long uid = userId != null ? userId : currentUserId();
        if (uid == null) {
            log.warn("聚合统计缺少用户标识, userId={}", userId);
            return AnalyticsVO.builder()
                    .fluency(0.0).pronunciation(0.0).wpm(0.0)
                    .fillerWords(new java.util.ArrayList<>())
                    .trend(new java.util.ArrayList<>())
                    .build();
        }
        log.info("获取语音聚合统计, userId={}, period={}", uid, period);
        return speechAnalysisService.getAnalytics(uid, period);
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
