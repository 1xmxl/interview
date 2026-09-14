package com.interview.controller;


import com.interview.domain.po.SessionAnswers;
import com.interview.service.ISessionAnswersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  面试回答控制器
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@RestController
@RequestMapping("/session-answers")
@RequiredArgsConstructor
@Slf4j
public class SessionAnswersController {

    private final ISessionAnswersService sessionAnswersService;

    /** 按会话 ID 查询回答列表 */
    @GetMapping("/list")
    public List<SessionAnswers> listBySession(@RequestParam Long sessionId) {
        log.info("查询会话回答列表, sessionId={}", sessionId);
        return sessionAnswersService.lambdaQuery().eq(SessionAnswers::getSessionId, sessionId).list();
    }

    /** 按题目 ID 查询回答（一道题一条回答） */
    @GetMapping("/question/{questionId}")
    public SessionAnswers getByQuestion(@PathVariable Long questionId) {
        log.info("按题目查询回答, questionId={}", questionId);
        return sessionAnswersService.lambdaQuery().eq(SessionAnswers::getQuestionId, questionId).one();
    }

    @GetMapping("/{id}")
    public SessionAnswers getById(@PathVariable Long id) {
        log.info("查询回答详情, id={}", id);
        return sessionAnswersService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody SessionAnswers sessionAnswers) {
        log.info("新增回答, sessionId={}, questionId={}", sessionAnswers.getSessionId(), sessionAnswers.getQuestionId());
        sessionAnswersService.save(sessionAnswers);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody SessionAnswers sessionAnswers) {
        log.info("更新回答, id={}", id);
        sessionAnswers.setId(id);
        sessionAnswersService.updateById(sessionAnswers);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除回答, id={}", id);
        sessionAnswersService.removeById(id);
    }
}
