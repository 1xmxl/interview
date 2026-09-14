package com.interview.controller;


import com.interview.domain.po.ResumeEducations;
import com.interview.service.IResumeEducationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  简历教育经历控制器
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@RestController
@RequestMapping("/resume-educations")
@RequiredArgsConstructor
@Slf4j
public class ResumeEducationsController {

    private final IResumeEducationsService resumeEducationsService;

    /** 按简历 ID 查询教育经历列表 */
    @GetMapping("/list")
    public List<ResumeEducations> listByResume(@RequestParam Long resumeId) {
        log.info("查询简历教育经历列表, resumeId={}", resumeId);
        return resumeEducationsService.lambdaQuery().eq(ResumeEducations::getResumeId, resumeId).list();
    }

    @GetMapping("/{id}")
    public ResumeEducations getById(@PathVariable Long id) {
        log.info("查询教育经历详情, id={}", id);
        return resumeEducationsService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody ResumeEducations resumeEducations) {
        log.info("新增教育经历: {}", resumeEducations.getInstitution());
        resumeEducationsService.save(resumeEducations);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ResumeEducations resumeEducations) {
        log.info("更新教育经历, id={}", id);
        resumeEducations.setId(id);
        resumeEducationsService.updateById(resumeEducations);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除教育经历, id={}", id);
        resumeEducationsService.removeById(id);
    }
}
