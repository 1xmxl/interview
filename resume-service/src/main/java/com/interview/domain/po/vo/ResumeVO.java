package com.interview.domain.po.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResumeVO {
    private Long id;
    private String originalFilename;   // 原始文件名（如 张三_简历.pdf）
    private String status;              // PENDING / PARSING / PARSED / FAILED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String coverName;           // 展示用的名字（可去掉后缀，或直接用原始名）
    private String fileType;            // 文件类型（如 pdf、docx、doc、txt）
}