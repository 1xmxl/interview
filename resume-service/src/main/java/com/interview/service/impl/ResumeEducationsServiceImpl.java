package com.interview.service.impl;

import com.interview.DTO.StructuredResume;
import com.interview.context.UserContext;
import com.interview.domain.po.ResumeEducations;
import com.interview.mapper.ResumeEducationsMapper;
import com.interview.service.IResumeEducationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@Service
public class ResumeEducationsServiceImpl extends ServiceImpl<ResumeEducationsMapper, ResumeEducations> implements IResumeEducationsService {

    @Override
    public void saveResumeEducations(List<StructuredResume.EducationEntry> educations, Long resumeId) {
        educations.forEach(education -> {
            String endDate = education.getEndDate();
            if(endDate==null){
                endDate= LocalDate.now().toString();
                education.setEndDate(endDate);
            }
            String startDate = education.getStartDate();
            if(startDate==null){
                startDate= LocalDate.now().toString();
                education.setStartDate(startDate);
            }
            ResumeEducations build = ResumeEducations.builder().resumeId(resumeId)
                    .endDate(LocalDate.parse(education.getEndDate()))
                    .fieldOfStudy(education.getFieldOfStudy())
                    .degree(education.getDegree())
                    .startDate(LocalDate.parse(education.getStartDate()))
                    .institution(education.getInstitution()).build();
            this.save(build);
        });
    }

    @Override
    public List<ResumeEducations> getListById(Long resumeId) {
        Long userId = Long.valueOf(UserContext.getUserId());
        List<ResumeEducations> list = lambdaQuery().eq(ResumeEducations::getResumeId, resumeId).list();
        return list;
    }

    @Override
    public void deleteAllById(Long resumeId) {
        List<ResumeEducations> list = lambdaQuery().eq(ResumeEducations::getResumeId, resumeId).list();
        List<Long> collect = list.stream().map(a -> {
            a.getId();
            return a.getId();
        }).collect(Collectors.toList());
        removeByIds(collect);
    }
}
