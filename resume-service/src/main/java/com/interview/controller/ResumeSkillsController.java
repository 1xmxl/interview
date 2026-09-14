package com.interview.controller;


import com.interview.domain.po.ResumeSkills;
import com.interview.service.IResumeSkillsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  简历技能控制器
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@RestController
@RequestMapping("/resume-skills")
@RequiredArgsConstructor
@Slf4j
public class ResumeSkillsController {

    private final IResumeSkillsService resumeSkillsService;

    /** 按简历 ID 查询技能列表 */
    @GetMapping("/list")
    public List<ResumeSkills> listByResume(@RequestParam Long resumeId) {
        log.info("查询简历技能列表, resumeId={}", resumeId);
        return resumeSkillsService.lambdaQuery().eq(ResumeSkills::getResumeId, resumeId).list();
    }

    @GetMapping("/{id}")
    public ResumeSkills getById(@PathVariable Long id) {
        log.info("查询技能详情, id={}", id);
        return resumeSkillsService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody ResumeSkills resumeSkills) {
        log.info("新增技能: {}", resumeSkills.getSkillName());
        resumeSkillsService.save(resumeSkills);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ResumeSkills resumeSkills) {
        log.info("更新技能, id={}", id);
        resumeSkills.setId(id);
        resumeSkillsService.updateById(resumeSkills);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除技能, id={}", id);
        resumeSkillsService.removeById(id);
    }
}
