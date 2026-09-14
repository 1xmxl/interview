package com.interview.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigSubbmit {
    private String name;
    private String interviewerStyle;
    private String difficulty;
    private List<String> targetSkills;
    private List<String> questionCategories;
    private String language;
}
