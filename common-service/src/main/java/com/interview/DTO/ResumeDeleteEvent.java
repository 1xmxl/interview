package com.interview.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResumeDeleteEvent {
    private Long resumeId;
    private Long userId;        // 操作者
    private String filePath;    // OSS 文件 key，供文件清理用
    private LocalDateTime deletedAt;
}