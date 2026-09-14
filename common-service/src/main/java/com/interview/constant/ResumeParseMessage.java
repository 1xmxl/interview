package com.interview.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeParseMessage {
    private Long resumeId;
    private Long userId;
    private String objectName;
}