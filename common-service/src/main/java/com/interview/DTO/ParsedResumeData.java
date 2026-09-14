package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResumeData {
    private String rawText;                     // 纯文本
    private List<StructuredResume.SkillEntry> skills;
    private List<StructuredResume.ExperienceEntry> experiences;
    private List<StructuredResume.ProjectEntry> projects;
    private List<StructuredResume.EducationEntry> educations;
}