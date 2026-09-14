package com.interview.controller;




import cn.hutool.core.bean.BeanUtil;
import com.interview.DTO.ParsedResumeData;
import com.interview.domain.po.ResumeExperiences;
import com.interview.domain.po.ResumeSkills;
import com.interview.domain.po.Resumes;
import com.interview.domain.po.vo.ResumeVO;
import com.interview.domain.vo.ParsedResumeDataVO;
import com.interview.service.IResumesService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Result;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumesController {
    private final IResumesService resumesService;
    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file
    , HttpServletRequest request
    ) throws IOException {
        log.info("start upload resume file");
        resumesService.upload(file);
    }
    @PutMapping("/{resumeId}/updateStatus")
    public void updateStatus(@PathVariable("resumeId") Long resumeId,
                             @RequestParam("userId") Long userId,
                             @RequestBody ParsedResumeData data){
        log.info("start update resume data, resumeId={}, userId={}", resumeId, userId);
        resumesService.updateStatus(resumeId, userId, data);
    }
    @GetMapping
    public List<ResumeVO> getResumes(){
        log.info("start get resumes");
        return resumesService.getResumes();
    }
    @GetMapping("/{resumeId}")
    public ParsedResumeDataVO getResume(@PathVariable("resumeId") Long resumeId){
        log.info("start get resume data");
        return resumesService.getResume(resumeId);

    }
    @DeleteMapping("/{resumeId}")
    public void deleteResume(@PathVariable("resumeId") Long resumeId){
        log.info("start delete resume data");
        resumesService.deleteResume(resumeId);
    }
    @GetMapping("/{resumeId}/skills")
    public List<ResumeSkills> getSkills(@PathVariable("resumeId") Long resumeId){
        log.info("start get skills data");
        return  resumesService.getSkills(resumeId);
    }
    @GetMapping("/{resumeId}/experiences")
    public List<ResumeExperiences>getExperiences(@PathVariable("resumeId") Long resumeId){
        log.info("start get experiences data");
        return resumesService.getExperiences(resumeId);
    }
}
