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
public class StructuredResume {
    private List<SkillEntry> skills;
    private List<ExperienceEntry> experiences;
    private List<ProjectEntry> projects;
    private List<EducationEntry> educations; // 可选

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillEntry {
        private String skillName;
        private String proficiency;   // BEGINNER/INTERMEDIATE/ADVANCED/EXPERT
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceEntry {
        private String company;
        private String position;
        private String startDate;     // yyyy-MM
        private String endDate;       // yyyy-MM 或 "至今"
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectEntry {
        private String projectName;
        private String description;
        private List<String> technologies;
        private String role;
        private String startDate;
        private String endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationEntry {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private String startDate;
        private String endDate;
    }
}