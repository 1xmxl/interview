package com.interview.controller;


import com.interview.domain.po.InterviewResults;
import com.interview.service.IInterviewResultsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  面试结果总评控制器
 * </p>
 *
 * @author author
 * @since 2026-07-20
 */
@RestController
@RequestMapping("/interview-results")
@RequiredArgsConstructor
@Slf4j
public class InterviewResultsController {

    private final IInterviewResultsService interviewResultsService;

    /** 按会话 ID 查询总评（一个会话一条） */
    @GetMapping("/session/{sessionId}")
    public InterviewResults getBySession(@PathVariable Long sessionId) {
        log.info("查询会话总评, sessionId={}", sessionId);
        return interviewResultsService.lambdaQuery()
                .eq(InterviewResults::getSessionId, sessionId).one();
    }

    /** 全部总评列表（按生成时间倒序） */
    @GetMapping("/list")
    public List<InterviewResults> listAll() {
        log.info("查询全部面试总评");
        return interviewResultsService.lambdaQuery()
                .orderByDesc(InterviewResults::getGeneratedAt).list();
    }

    @GetMapping("/{id}")
    public InterviewResults getById(@PathVariable Long id) {
        log.info("查询总评详情, id={}", id);
        return interviewResultsService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody InterviewResults interviewResults) {
        log.info("新增面试总评, sessionId={}", interviewResults.getSessionId());
        interviewResultsService.save(interviewResults);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody InterviewResults interviewResults) {
        log.info("更新面试总评, id={}", id);
        interviewResults.setId(id);
        interviewResultsService.updateById(interviewResults);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除面试总评, id={}", id);
        interviewResultsService.removeById(id);
    }
}
