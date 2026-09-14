package com.interview.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.interview.DTO.ParsedResumeData;
import com.interview.DTO.ResumeDeleteEvent;
import com.interview.DTO.StructuredResume;
import com.interview.constant.RabbitMQConstant;
import com.interview.constant.ResumeParseMessage;
import com.interview.context.UserContext;
import com.interview.domain.po.*;
import com.interview.domain.po.vo.ResumeVO;
import com.interview.domain.vo.ParsedResumeDataVO;
import com.interview.mapper.ResumesMapper;
import com.interview.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.util.OssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumesServiceImpl extends ServiceImpl<ResumesMapper, Resumes> implements IResumesService {

    private final OssUtil ossUtil;
    private final RabbitTemplate rabbitTemplate;
    private final IResumeEducationsService iResumeEducationsService;
    private final IResumeExperiencesService iResumeExperiencesService;
    private final IResumeProjectsService iResumeProjectsService;
    private final IResumeSkillsService  iResumeSkillsService;
    @Override
    public void upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (!originalFilename.toLowerCase().matches(".*\\.(pdf|doc|docx|txt|rtf|odt|md|html)$")) {
            throw new IllegalArgumentException("不支持的文件格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
        String uid = UUID.randomUUID().toString()+'_'+originalFilename;
        ossUtil.uploadFile(file, uid);
        Long userId = Long.valueOf(UserContext.getUserId());
        Resumes resumes = new Resumes();
        resumes.setFilePath(uid)
                .setCreatedAt(LocalDateTime.now())
                .setOriginalFilename(originalFilename)
                .setUpdatedAt(LocalDateTime.now())
                .setUserId(userId)
                .setStatus("PENDING");
        save(resumes);
        Long id = resumes.getId();
        ResumeParseMessage message = new ResumeParseMessage(id, userId, uid);
        rabbitTemplate.convertAndSend(RabbitMQConstant.Exchange.RESUME_EXCHANGE,RabbitMQConstant.Key.RESUME_PARSE_LEY,message);
    }

    @Override
    public void updateStatus(Long resumeId, Long userId, ParsedResumeData data) {
       // log.info("start update resume data, resumeId={}, userId={}", resumeId, userId);
        List<StructuredResume.EducationEntry> educations = data.getEducations();
        List<StructuredResume.ExperienceEntry> experiences = data.getExperiences();
        List<StructuredResume.ProjectEntry> projects = data.getProjects();
        List<StructuredResume.SkillEntry> skills = data.getSkills();
        addEmptyIfEmpty(educations, () -> StructuredResume.EducationEntry.builder().build());
        addEmptyIfEmpty(experiences, () -> StructuredResume.ExperienceEntry.builder().build());
        addEmptyIfEmpty(projects, () -> StructuredResume.ProjectEntry.builder().build());
        addEmptyIfEmpty(skills, () -> StructuredResume.SkillEntry.builder().build());
        String content = data.getRawText();
        lambdaUpdate().eq(Resumes::getId, resumeId)
                .eq(Resumes::getUserId, userId)
                .set(Resumes::getStatus,"PARSED")
                .set(Resumes::getParsedText, content).update();
        saveAllTable(resumeId, educations, experiences, projects, skills);

    }

    @Override
    public List<ResumeVO> getResumes() {
        Long userId =Long.valueOf(UserContext.getUserId());
        List<Resumes> list = lambdaQuery().eq(Resumes::getUserId, userId).list();
        List<ResumeVO> resumeVOs=new ArrayList<>(list.size());
        list.forEach(resumes -> {
            ResumeVO resumeVO = BeanUtil.copyProperties(resumes, ResumeVO.class);
            String originalFilename = resumes.getOriginalFilename();
            int index = originalFilename.lastIndexOf(".");
            resumeVO.setCoverName(originalFilename.substring(0, index));
            resumeVO.setFileType(originalFilename.substring(index + 1));
            resumeVOs.add(resumeVO);
        });
        return resumeVOs;
    }

    @Override
    public ParsedResumeDataVO getResume(Long resumeId) {
        Long userId =Long.valueOf(UserContext.getUserId());
        Resumes one = lambdaQuery().eq(Resumes::getUserId, userId)
                .eq(Resumes::getId, resumeId).one();
        ParsedResumeDataVO parsedResumeData = ParsedResumeDataVO.builder().experiences(new ArrayList<>())
                .educations(new ArrayList<>())
                .skills(new ArrayList<>())
                .projects(new ArrayList<>()).build();
        transferToParsedResumeData(resumeId,parsedResumeData,one);
        return parsedResumeData;
    }

    @Override
    public void deleteResume(Long resumeId) {
        Resumes one = lambdaQuery().eq(Resumes::getId, resumeId)
                .one();
        removeById(resumeId);
        deleteRelationData(resumeId);
        ResumeDeleteEvent message = ResumeDeleteEvent.builder().userId(Long.valueOf(UserContext.getUserId())).resumeId(resumeId)
                .deletedAt(LocalDateTime.now())
                .filePath(one.getFilePath())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstant.Exchange.RESUME_EXCHANGE,RabbitMQConstant.Key.RESUME_DELETE_KEY,message);
        log.info("已发送简历删除事件，resumeId={}", resumeId);
    }

    @Override
    public List<ResumeSkills> getSkills(Long resumeId) {
        List<ResumeSkills> listById = iResumeSkillsService.getListById(resumeId);
        return listById;
    }

    @Override
    public List<ResumeExperiences> getExperiences(Long resumeId) {
        return iResumeExperiencesService.getListById(resumeId);
    }

    private void deleteRelationData(Long resumeId) {
        iResumeEducationsService.deleteAllById(resumeId);
        iResumeSkillsService.deleteAllById(resumeId);
        iResumeProjectsService.deleteAllById(resumeId);
        iResumeExperiencesService.deleteAllById(resumeId);
    }

    private void transferToParsedResumeData(Long resumeId, ParsedResumeDataVO parsedResumeData, Resumes one) {
        List<ResumeEducations>resumeEducationsList= iResumeEducationsService.getListById(resumeId);
        resumeEducationsList.forEach(resumeEducations -> {
            StructuredResume.EducationEntry educationEntry = BeanUtil.copyProperties(resumeEducations, StructuredResume.EducationEntry.class);
            List<StructuredResume.EducationEntry> educations = parsedResumeData.getEducations();
            educationEntry.setStartDate(resumeEducations.getStartDate().toString());
            educationEntry.setEndDate(resumeEducations.getEndDate().toString());
            educations.add(educationEntry);
        });
        List<ResumeSkills>resumeSkills=iResumeSkillsService.getListById(resumeId);
        resumeSkills.forEach(a->{
            StructuredResume.SkillEntry skillEntry = BeanUtil.copyProperties(a, StructuredResume.SkillEntry.class);
            List<StructuredResume.SkillEntry> skills = parsedResumeData.getSkills();
            skills.add(skillEntry);
        });

        List<ResumeExperiences>resumeExperiencesList= iResumeExperiencesService.getListById(resumeId);
        resumeExperiencesList.forEach(resumeExperiences -> {
            StructuredResume.ExperienceEntry experienceEntry = BeanUtil.copyProperties(resumeExperiences, StructuredResume.ExperienceEntry.class);
            List<StructuredResume.ExperienceEntry> experiences = parsedResumeData.getExperiences();
            experienceEntry.setStartDate(resumeExperiences.getStartDate().toString());
            experienceEntry.setEndDate(resumeExperiences.getEndDate().toString());
            experiences.add(experienceEntry);
        });
        List<ResumeProjects>resumeProjectsList=iResumeProjectsService.getListById(resumeId);
        resumeProjectsList.forEach(resumeProjects -> {
            StructuredResume.ProjectEntry projectEntry = BeanUtil.copyProperties(resumeProjects, StructuredResume.ProjectEntry.class);
            List<StructuredResume.ProjectEntry> projects = parsedResumeData.getProjects();
            projectEntry.setStartDate(resumeProjects.getStartDate().toString());
            projectEntry.setEndDate(resumeProjects.getEndDate().toString());
            projects.add(projectEntry);
        });
        parsedResumeData.setResumeId(resumeId);
        String originalFilename = one.getOriginalFilename();
        String status = one.getStatus();
        LocalDateTime createdAt = one.getCreatedAt();
        LocalDateTime updatedAt = one.getUpdatedAt();
        parsedResumeData.setCreatedAt(createdAt);
        parsedResumeData.setStatus(status);
        parsedResumeData.setOriginalFilename(originalFilename);
        parsedResumeData.setUpdateAt(updatedAt);

    }

    @NotNull
    private ParsedResumeData getParsedResumeData(Long resumeId, List<ResumeEducations> resumeEducationsList) {
        ParsedResumeData parsedResumeData = new ParsedResumeData();
        resumeEducationsList.forEach(resumeEducations -> {
            StructuredResume.EducationEntry educationEntry = BeanUtil.copyProperties(resumeEducations, StructuredResume.EducationEntry.class);
            List<StructuredResume.EducationEntry> educations = parsedResumeData.getEducations();
            educationEntry.setStartDate(resumeEducations.getStartDate().toString());
            educationEntry.setEndDate(resumeEducations.getEndDate().toString());
            educations.add(educationEntry);
        });
        List<ResumeExperiences>resumeExperiencesList= iResumeExperiencesService.getListById(resumeId);
        resumeExperiencesList.forEach(resumeExperiences -> {
            StructuredResume.ExperienceEntry experienceEntry = BeanUtil.copyProperties(resumeExperiences, StructuredResume.ExperienceEntry.class);
            List<StructuredResume.ExperienceEntry> experiences = parsedResumeData.getExperiences();
            experienceEntry.setStartDate(resumeExperiences.getStartDate().toString());
            experienceEntry.setEndDate(resumeExperiences.getEndDate().toString());
            experiences.add(experienceEntry);
        });
        List<ResumeProjects>resumeProjectsList=iResumeProjectsService.getListById(resumeId);
        resumeProjectsList.forEach(resumeProjects -> {
            StructuredResume.ProjectEntry projectEntry = BeanUtil.copyProperties(resumeProjects, StructuredResume.ProjectEntry.class);
            List<StructuredResume.ProjectEntry> projects = parsedResumeData.getProjects();
            projectEntry.setStartDate(resumeProjects.getStartDate().toString());
            projectEntry.setEndDate(resumeProjects.getEndDate().toString());
            projects.add(projectEntry);
        });
        return parsedResumeData;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void saveAllTable(Long resumeId, List<StructuredResume.EducationEntry> educations, List<StructuredResume.ExperienceEntry> experiences, List<StructuredResume.ProjectEntry> projects, List<StructuredResume.SkillEntry> skills) {
        iResumeEducationsService.saveResumeEducations(educations, resumeId);
        iResumeExperiencesService.saveResumeExperiences(experiences, resumeId);
        iResumeProjectsService.saveResumeProjects(projects, resumeId);
        iResumeSkillsService.saveResumeSkills(skills, resumeId);
    }
    private <T> void addEmptyIfEmpty(List<T> list, Supplier<T> emptySupplier) {
        if (list.isEmpty()) {
            list.add(emptySupplier.get());
        }
    }
    private <T> void ensureNonEmpty(List<T> list, Supplier<T> instanceSupplier) {
        if (list.isEmpty()) {
            list.add(instanceSupplier.get());
        }
    }
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(RabbitMQConstant.Queue.RESUME_DELETE_QUEUE),
                    exchange = @Exchange(RabbitMQConstant.Exchange.RESUME_EXCHANGE),
                    key = RabbitMQConstant.Key.RESUME_DELETE_KEY
            )
    )
    public void OssDelete(ResumeDeleteEvent resumeDeleteEvent) {
        try {
            String filePath = resumeDeleteEvent.getFilePath();
            ossUtil.deleteOssObject(filePath);
            log.info("已删除 OSS 文件：{}，resumeId={}", resumeDeleteEvent.getFilePath(), resumeDeleteEvent.getResumeId());
        } catch (Exception e) {
            log.error("删除 OSS 文件失败，resumeId={}, filePath={}", resumeDeleteEvent.getResumeId(), resumeDeleteEvent.getFilePath(), e);
            throw new RuntimeException(e);
        }
    }


}
