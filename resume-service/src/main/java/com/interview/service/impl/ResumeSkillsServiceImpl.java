package com.interview.service.impl;

import com.interview.DTO.StructuredResume;
import com.interview.domain.po.ResumeSkills;
import com.interview.mapper.ResumeSkillsMapper;
import com.interview.service.IResumeSkillsService;
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
public class ResumeSkillsServiceImpl extends ServiceImpl<ResumeSkillsMapper, ResumeSkills> implements IResumeSkillsService {

    @Override
    public void saveResumeSkills(List<StructuredResume.SkillEntry> skills, Long resumeId) {
        skills.forEach(skill -> {

            ResumeSkills build = ResumeSkills.builder().resumeId(resumeId)
                    .skillName(skill.getSkillName())
                    .category(skill.getCategory())
                    .proficiency(skill.getProficiency())
                    .build();
            save(build);
        });
    }

    @Override
    public List<ResumeSkills> getListById(Long resumeId) {
        List<ResumeSkills> list = lambdaQuery().eq(ResumeSkills::getResumeId, resumeId).list();

        return list;
    }

    @Override
    public void deleteAllById(Long resumeId) {
        List<ResumeSkills> list = lambdaQuery().eq(ResumeSkills::getResumeId, resumeId).list();
        List<Long> collect = list
                .stream().map(a -> {
                    a.getId();
                    return a.getId();
                }).collect(Collectors.toList());
        removeByIds(collect);
    }
}
