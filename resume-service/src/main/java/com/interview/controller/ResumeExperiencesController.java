package com.interview.controller;


import com.interview.domain.po.ResumeExperiences;
import com.interview.service.IResumeExperiencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  简历工作经历控制器
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@RestController
@RequestMapping("/resume-experiences")
@RequiredArgsConstructor
@Slf4j
public class ResumeExperiencesController {

    private final IResumeExperiencesService resumeExperiencesService;

    /** 按简历 ID 查询工作经历列表 */
    @GetMapping("/list")
    public List<ResumeExperiences> listByResume(@RequestParam Long resumeId) {
        log.info("查询简历工作经历列表, resumeId={}", resumeId);
        return resumeExperiencesService.lambdaQuery().eq(ResumeExperiences::getResumeId, resumeId).list();
    }

    @GetMapping("/{id}")
    public ResumeExperiences getById(@PathVariable Long id) {
        log.info("查询工作经历详情, id={}", id);
        return resumeExperiencesService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody ResumeExperiences resumeExperiences) {
        log.info("新增工作经历: {}-{}", resumeExperiences.getCompany(), resumeExperiences.getPosition());
        resumeExperiencesService.save(resumeExperiences);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ResumeExperiences resumeExperiences) {
        log.info("更新工作经历, id={}", id);
        resumeExperiences.setId(id);
        resumeExperiencesService.updateById(resumeExperiences);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除工作经历, id={}", id);
        resumeExperiencesService.removeById(id);
    }
}
