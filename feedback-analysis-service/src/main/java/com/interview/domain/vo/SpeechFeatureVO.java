package com.interview.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语音分析详情单项（与前端 feedback/speech.vue 对齐：{feature, value, unit}）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechFeatureVO {
    private String feature;
    private Double value;
    private String unit;
}
