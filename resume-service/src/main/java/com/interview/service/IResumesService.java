package com.interview.service;

import com.interview.DTO.ParsedResumeData;
import com.interview.domain.po.ResumeExperiences;
import com.interview.domain.po.ResumeSkills;
import com.interview.domain.po.Resumes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.domain.po.vo.ResumeVO;
import com.interview.domain.vo.ParsedResumeDataVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
public interface IResumesService extends IService<Resumes> {

    void upload(MultipartFile file) throws IOException;

    void updateStatus(@PathVariable Long resumeId, @RequestParam Long userId, @RequestBody ParsedResumeData data);

    List<ResumeVO> getResumes();

    ParsedResumeDataVO getResume(Long resumeId);

    void deleteResume(Long resumeId);

    List<ResumeSkills> getSkills(Long resumeId);

    List<ResumeExperiences> getExperiences(Long resumeId);
}
