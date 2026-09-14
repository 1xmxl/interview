package com.interview.service;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeExperiences;
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
public interface IResumeExperiencesService extends IService<ResumeExperiences> {
    void saveResumeExperiences(List<StructuredResume.ExperienceEntry> experiences,Long resumeId);

    List<ResumeExperiences> getListById(Long resumeId);

    void deleteAllById(Long resumeId);
}
