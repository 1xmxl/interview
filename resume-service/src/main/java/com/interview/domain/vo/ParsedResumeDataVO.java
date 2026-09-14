package com.interview.domain.vo;

import com.interview.DTO.StructuredResume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResumeDataVO {
    private Long resumeId;
    private String originalFilename;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private String rawText;                     // 纯文本
    private List<StructuredResume.SkillEntry> skills;
    private List<StructuredResume.ExperienceEntry> experiences;
    private List<StructuredResume.ProjectEntry> projects;
    private List<StructuredResume.EducationEntry> educations;
}