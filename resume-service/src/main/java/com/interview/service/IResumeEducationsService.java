package com.interview.service;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeEducations;
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
public interface IResumeEducationsService extends IService<ResumeEducations> {
    void saveResumeEducations(List<StructuredResume.EducationEntry> resumeEducations,Long resumeId);

    List<ResumeEducations> getListById(Long resumeId);

    void deleteAllById(Long resumeId);
}
