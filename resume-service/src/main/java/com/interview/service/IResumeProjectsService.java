package com.interview.service;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeProjects;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
public interface IResumeProjectsService extends IService<ResumeProjects> {
    void saveResumeProjects(List<StructuredResume.ProjectEntry> resumeProjects , Long resumeId);

    List<ResumeProjects> getListById(Long resumeId);

    void deleteAllById(Long resumeId);
}
