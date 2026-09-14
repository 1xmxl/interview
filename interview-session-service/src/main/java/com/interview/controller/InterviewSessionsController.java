package com.interview.controller;


import com.interview.domain.dto.SessionSubmit;
import com.interview.domain.po.InterviewSessions;
import com.interview.domain.vo.SessionHistoryVO;
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
    public void createSession(@RequestBody SessionSubmit submit) {
        log.info("创建面试会话, configId={}, resumeId={}", submit.getConfigId(), submit.getResumeId());
        interviewSessionsService.createSession(submit);
    }

    @GetMapping("/sessions/{id}")
    public InterviewSessions getSession(@PathVariable Long id) {
        log.info("获取会话状态, id={}", id);
        return interviewSessionsService.getSession(id);
    }

    @GetMapping("/sessions/{id}/history")
    public List<SessionHistoryVO> getHistory(@PathVariable Long id) {
        log.info("获取会话问答历史, id={}", id);
        return interviewSessionsService.getHistory(id);
    }
}
