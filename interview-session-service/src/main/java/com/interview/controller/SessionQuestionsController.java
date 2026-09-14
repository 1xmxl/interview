package com.interview.controller;


import com.interview.domain.po.SessionQuestions;
import com.interview.service.ISessionQuestionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  面试题目控制器
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@RestController
@RequestMapping("/session-questions")
@RequiredArgsConstructor
@Slf4j
public class SessionQuestionsController {

    private final ISessionQuestionsService sessionQuestionsService;

    /** 按会话 ID 查询题目列表（按题目顺序排序） */
    @GetMapping("/list")
    public List<SessionQuestions> listBySession(@RequestParam Long sessionId) {
        log.info("查询会话题目列表, sessionId={}", sessionId);
        return sessionQuestionsService.lambdaQuery()
                .eq(SessionQuestions::getSessionId, sessionId)
                .orderByAsc(SessionQuestions::getOrderIndex)
                .list();
    }

    @GetMapping("/{id}")
    public SessionQuestions getById(@PathVariable Long id) {
        log.info("查询题目详情, id={}", id);
        return sessionQuestionsService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody SessionQuestions sessionQuestions) {
        log.info("新增题目, sessionId={}", sessionQuestions.getSessionId());
        sessionQuestionsService.save(sessionQuestions);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody SessionQuestions sessionQuestions) {
        log.info("更新题目, id={}", id);
        sessionQuestions.setId(id);
        sessionQuestionsService.updateById(sessionQuestions);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除题目, id={}", id);
        sessionQuestionsService.removeById(id);
    }
}
