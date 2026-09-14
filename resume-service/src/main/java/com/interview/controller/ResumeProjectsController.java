package com.interview.controller;


import com.interview.domain.po.ResumeProjects;
import com.interview.service.IResumeProjectsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  简历项目经历控制器
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@RestController
@RequestMapping("/resume-projects")
@RequiredArgsConstructor
@Slf4j
public class ResumeProjectsController {

    private final IResumeProjectsService resumeProjectsService;

    /** 按简历 ID 查询项目经历列表 */
    @GetMapping("/list")
    public List<ResumeProjects> listByResume(@RequestParam Long resumeId) {
        log.info("查询简历项目列表, resumeId={}", resumeId);
        return resumeProjectsService.lambdaQuery().eq(ResumeProjects::getResumeId, resumeId).list();
    }

    @GetMapping("/{id}")
    public ResumeProjects getById(@PathVariable Long id) {
        log.info("查询项目详情, id={}", id);
        return resumeProjectsService.getById(id);
    }

    @PostMapping
    public void save(@RequestBody ResumeProjects resumeProjects) {
        log.info("新增项目: {}", resumeProjects.getProjectName());
        resumeProjectsService.save(resumeProjects);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ResumeProjects resumeProjects) {
        log.info("更新项目, id={}", id);
        resumeProjects.setId(id);
        resumeProjectsService.updateById(resumeProjects);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("删除项目, id={}", id);
        resumeProjectsService.removeById(id);
    }
}
