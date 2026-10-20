package com.interview.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 语音分析结果文档（对应 ES 索引 speech_analytics）。
 * timestamp 存 epoch millis（long），便于 Java 侧聚合与范围查询。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechAnalysisDoc {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("question_id")
    private Long questionId;

    @JsonProperty("answer_id")
    private Long answerId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("audio_duration_sec")
    private Double audioDurationSec;

    @JsonProperty("transcript_text")
    private String transcriptText;

    @JsonProperty("word_count")
    private Integer wordCount;

    @JsonProperty("words_per_minute")
    private Double wordsPerMinute;

    @JsonProperty("pauses")
    private List<PauseInfo> pauses;

    @JsonProperty("filler_words")
    private Map<String, Integer> fillerWords;

    @JsonProperty("fluency_score")
    private Double fluencyScore;

    @JsonProperty("clarity_score")
    private Double clarityScore;

    @JsonProperty("timestamp")
    private Long timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PauseInfo {
        @JsonProperty("start_sec")
        private Double startSec;

        @JsonProperty("duration_sec")
        private Double durationSec;
    }
}
