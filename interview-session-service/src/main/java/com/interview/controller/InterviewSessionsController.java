package com.interview.controller;


import com.interview.DTO.SessionResultDTO;
import com.interview.DTO.SessionStatsDTO;
import com.interview.domain.dto.AnswerSubmit;
import com.interview.domain.dto.SessionSubmit;
import com.interview.domain.po.InterviewSessions;
import com.interview.domain.vo.NextQuestionVO;
import com.interview.domain.vo.SessionHistoryVO;
import com.interview.domain.vo.StartQuestionVO;
import com.interview.service.IInterviewSessionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@RestController
@RequestMapping("/interviews")
@Slf4j
@RequiredArgsConstructor
public class InterviewSessionsController {

    private final IInterviewSessionsService interviewSessionsService;

    @PostMapping("/sessions")
    public Long createSession(@RequestBody SessionSubmit submit) {
        log.info("创建面试会话, configId={}, resumeId={}", submit.getConfigId(), submit.getResumeId());
        return interviewSessionsService.createSession(submit);
    }

    @GetMapping("/sessions/{id}")
    public InterviewSessions getSession(@PathVariable("id") Long id) {
        log.info("获取会话状态, id={}", id);
        return interviewSessionsService.getSession(id);
    }

    @GetMapping("/sessions/{id}/history")
    public List<SessionHistoryVO> getHistory(@PathVariable("id") Long id) {
        log.info("获取会话问答历史, id={}", id);
        return interviewSessionsService.getHistory(id);
    }

    @PostMapping("/sessions/{id}/start")
    public StartQuestionVO start(@PathVariable("id") Long id) {
        log.info("开始面试, id={}", id);
        return interviewSessionsService.start(id);
    }

    @PostMapping("/sessions/{id}/answer")
    public NextQuestionVO answer(@PathVariable("id") Long id, @RequestBody AnswerSubmit submit) {
        log.info("提交答案, id={}", id);
        return interviewSessionsService.answer(id, submit);
    }

    @PostMapping("/sessions/{id}/end")
    public void end(@PathVariable("id") Long id) {
        log.info("结束面试, id={}", id);
        interviewSessionsService.end(id);
    }

    /** 回写评估结果（Feign 供 ai-evaluation-service 调用） */
    @PutMapping("/sessions/{id}/result")
    public void updateResult(@PathVariable("id") Long id,
                             @RequestParam("userId") Long userId,
                             @RequestBody SessionResultDTO result) {
        log.info("回写评估结果, id={}, userId={}", id, userId);
        interviewSessionsService.updateResult(id, userId, result);
    }

    /** 当前用户面试统计（首页数据概览） */
    @GetMapping("/stats")
    public SessionStatsDTO getStats() {
        log.info("获取面试统计");
        return interviewSessionsService.getStats();
    }
}
