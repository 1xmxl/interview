package com.interview.service.impl;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeExperiences;
import com.interview.mapper.ResumeExperiencesMapper;
import com.interview.service.IResumeExperiencesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class ResumeExperiencesServiceImpl extends ServiceImpl<ResumeExperiencesMapper, ResumeExperiences> implements IResumeExperiencesService {


    @Override
    public void saveResumeExperiences(List<StructuredResume.ExperienceEntry> experiences,Long resumeId) {
        experiences.forEach(experience -> {
            String endDate = experience.getEndDate();
            if(endDate==null){
                endDate= LocalDate.now().toString();
                experience.setEndDate(endDate);
            }
            String startDate = experience.getStartDate();
            if(startDate==null){
                startDate= LocalDate.now().toString();
                experience.setStartDate(startDate);
            }
            ResumeExperiences build = ResumeExperiences.builder()
                    .resumeId(resumeId)
                    .description(experience.getDescription())
                    .endDate(LocalDate.parse(experience.getEndDate()))
                    .startDate(LocalDate.parse(experience.getStartDate()))
                    .company(experience.getCompany())
                    .position(experience.getPosition())
                    .build();
            save(build);
        });
    }

    @Override
    public List<ResumeExperiences> getListById(Long resumeId) {
        List<ResumeExperiences> list = lambdaQuery().eq(ResumeExperiences::getResumeId, resumeId).list();
        return list;
    }

    @Override
    public void deleteAllById(Long resumeId) {
        List<Long> collect = lambdaQuery().eq(ResumeExperiences::getResumeId, resumeId).list().stream().map(a -> {
            return a.getId();
        }).collect(Collectors.toList());
        removeByIds(collect);
    }
}
