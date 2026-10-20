package com.interview.service;

import com.interview.DTO.SpeechAnalysisMessage;
import com.interview.domain.po.SpeechAnalysisDoc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 语音指标计算器。
 *
 * <p>基于转写文本计算：总词数、语速（词/分钟）、填充词、流利度、清晰度。
 * 音频层面的停顿检测需要语音识别服务（DashScope paraformer），当前版本先以文本指标为主，
 * 后续接入音频转写后可补充真实停顿数据。
 */
@Component
public class SpeechAnalysisCalculator {

    /** 中文填充词 + 英文填充词（统一小写匹配） */
    private static final List<String> FILLER_WORDS = List.of(
            "嗯", "呃", "啊", "那个", "然后", "就是", "这个", "um", "uh", "ah", "like", "you know");

    public SpeechAnalysisDoc calculate(SpeechAnalysisMessage message) {
        String text = message.getAnswerText() == null ? "" : message.getAnswerText().trim();

        int wordCount = countWords(text);
        // 没有音频时长时按中文语速 ~3.5 字/秒估算，仅用于演示展示
        double durationSec = wordCount > 0 ? Math.max(3.0, wordCount / 3.5) : 0.0;
        double wpm = durationSec > 0 ? round1(wordCount / durationSec * 60.0) : 0.0;

        Map<String, Integer> fillerWords = countFillerWords(text);
        int fillerTotal = fillerWords.values().stream().mapToInt(Integer::intValue).sum();

        double fluency = wordCount > 0
                ? clamp(round1(100 - fillerTotal * 4.0 - (wpm < 110 ? 10 : 0) - (wpm > 240 ? 8 : 0)), 0, 98)
                : 0.0;
        double clarity = wordCount > 0
                ? clamp(round1(60 + Math.min(38, wordCount / 40.0)), 0, 98)
                : 0.0;

        return SpeechAnalysisDoc.builder()
                .sessionId(message.getSessionId())
                .questionId(message.getQuestionId())
                .answerId(message.getAnswerId())
                .userId(message.getUserId())
                .audioDurationSec(round1(durationSec))
                .transcriptText(text)
                .wordCount(wordCount)
                .wordsPerMinute(wpm)
                .pauses(new ArrayList<>())
                .fillerWords(fillerWords)
                .fluencyScore(fluency)
                .clarityScore(clarity)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 词数：中文字符按 1 词计，英文按空格分词计。 */
    private int countWords(String text) {
        if (text.isBlank()) {
            return 0;
        }
        int chinese = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chinese++;
            }
        }
        String latin = text.replaceAll("[^\\x00-\\x7F]", " ").trim();
        int latinWords = latin.isEmpty() ? 0 : latin.split("\\s+").length;
        return chinese + latinWords;
    }

    private Map<String, Integer> countFillerWords(String text) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (text.isBlank()) {
            return counts;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String word : FILLER_WORDS) {
            int count = countOccurrences(lower, word.toLowerCase(Locale.ROOT));
            if (count > 0) {
                counts.put(word, count);
            }
        }
        return counts;
    }

    private int countOccurrences(String text, String word) {
        if (word.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) >= 0) {
            count++;
            idx += word.length();
        }
        return count;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
