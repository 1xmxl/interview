package com.interview.service;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeSkills;
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
public interface IResumeSkillsService extends IService<ResumeSkills> {
    void saveResumeSkills(List<StructuredResume.SkillEntry> skills,Long resumeId);

    List<ResumeSkills> getListById(Long resumeId);

    void deleteAllById(Long resumeId);
}
