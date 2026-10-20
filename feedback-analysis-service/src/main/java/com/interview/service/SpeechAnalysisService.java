package com.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.interview.domain.po.SpeechAnalysisDoc;
import com.interview.domain.vo.AnalyticsVO;
import com.interview.domain.vo.SpeechFeatureVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 语音分析查询服务：基于低层 RestClient 访问 ES speech_analytics 索引（兼容 ES 7.x）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeechAnalysisService {

    public static final String SPEECH_ANALYTICS_INDEX = "speech_analytics";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    /** 获取某次面试的语音分析详情（前端展示为特征卡片列表）。 */
    public List<SpeechFeatureVO> getSpeechDetail(Long sessionId, Long userId) {
        List<SpeechAnalysisDoc> docs = searchBySession(sessionId, userId);
        if (docs.isEmpty()) {
            return new ArrayList<>();
        }
        double avgWpm = avg(docs, SpeechAnalysisDoc::getWordsPerMinute);
        double avgFluency = avg(docs, SpeechAnalysisDoc::getFluencyScore);
        double avgClarity = avg(docs, SpeechAnalysisDoc::getClarityScore);
        double avgPauseMs = avgPauseMillis(docs);
        int totalWords = docs.stream().mapToInt(d -> d.getWordCount() == null ? 0 : d.getWordCount()).sum();
        double totalDuration = docs.stream()
                .mapToDouble(d -> d.getAudioDurationSec() == null ? 0 : d.getAudioDurationSec())
                .sum();

        List<SpeechFeatureVO> features = new ArrayList<>();
        features.add(SpeechFeatureVO.builder().feature("语速 (词/分钟)").value(round1(avgWpm)).unit("词/分").build());
        features.add(SpeechFeatureVO.builder().feature("流利度").value(round1(avgFluency)).unit("分").build());
        features.add(SpeechFeatureVO.builder().feature("清晰度").value(round1(avgClarity)).unit("分").build());
        features.add(SpeechFeatureVO.builder().feature("平均停顿 (ms)").value(round0(avgPauseMs)).unit("ms").build());
        features.add(SpeechFeatureVO.builder().feature("总词数").value((double) totalWords).unit("词").build());
        features.add(SpeechFeatureVO.builder().feature("录音时长").value(round0(totalDuration)).unit("秒").build());
        return features;
    }

    /** 用户语音能力聚合统计（按周期，如 30d / 6m / 1y）。 */
    public AnalyticsVO getAnalytics(Long userId, String period) {
        long startMillis = System.currentTimeMillis() - periodMillis(period);
        List<SpeechAnalysisDoc> docs = searchByUser(userId, startMillis);
        if (docs.isEmpty()) {
            return AnalyticsVO.builder()
                    .fluency(0.0).pronunciation(0.0).wpm(0.0)
                    .fillerWords(new ArrayList<>())
                    .trend(new ArrayList<>())
                    .build();
        }
        double fluency = avg(docs, SpeechAnalysisDoc::getFluencyScore);
        double pronunciation = avg(docs, SpeechAnalysisDoc::getClarityScore);
        double wpm = avg(docs, SpeechAnalysisDoc::getWordsPerMinute);

        Map<String, Integer> fillerTotal = new LinkedHashMap<>();
        for (SpeechAnalysisDoc doc : docs) {
            if (doc.getFillerWords() == null) {
                continue;
            }
            doc.getFillerWords().forEach((word, count) ->
                    fillerTotal.merge(word, count == null ? 0 : count, Integer::sum));
        }
        List<AnalyticsVO.FillerWordVO> fillerWords = fillerTotal.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> AnalyticsVO.FillerWordVO.builder().word(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());

        // 按天聚合流利度趋势，最多保留最近 7 个点
        Map<String, List<Double>> byDay = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (SpeechAnalysisDoc doc : docs) {
            if (doc.getFluencyScore() == null || doc.getTimestamp() == null) {
                continue;
            }
            String day = Instant.ofEpochMilli(doc.getTimestamp())
                    .atZone(ZoneId.systemDefault()).toLocalDate().format(fmt);
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(doc.getFluencyScore());
        }
        List<Integer> trend = byDay.values().stream()
                .map(vals -> (int) Math.round(vals.stream().mapToDouble(Double::doubleValue).average().orElse(0)))
                .collect(Collectors.toList());
        if (trend.size() > 7) {
            trend = trend.subList(trend.size() - 7, trend.size());
        }

        return AnalyticsVO.builder()
                .fluency(round1(fluency))
                .pronunciation(round1(pronunciation))
                .wpm(round1(wpm))
                .fillerWords(fillerWords)
                .trend(trend)
                .build();
    }

    /** 按 answer_id 判重，避免重复消费。 */
    public boolean existsByAnswerId(Long answerId) {
        try {
            ObjectNode term = objectMapper.createObjectNode();
            term.putObject("term").put("answer_id", answerId);
            List<SpeechAnalysisDoc> docs = search(term, 1);
            return !docs.isEmpty();
        } catch (Exception e) {
            log.warn("查询 answer_id 去重失败, answerId={}, 按不存在处理: {}", answerId, e.getMessage());
            return false;
        }
    }

    /** 写入分析结果（answerId 作为文档 ID，天然幂等）。 */
    public void save(SpeechAnalysisDoc doc) {
        try {
            Request request = new Request("PUT", "/" + SPEECH_ANALYTICS_INDEX + "/_doc/" + doc.getAnswerId());
            request.setJsonEntity(objectMapper.writeValueAsString(doc));
            Response response = restClient.performRequest(request);
            int status = response.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("写入 ES 失败, status=" + status);
            }
        } catch (Exception e) {
            log.error("写入语音分析结果失败, answerId={}", doc.getAnswerId(), e);
            throw new RuntimeException("写入语音分析结果失败", e);
        }
    }

    private List<SpeechAnalysisDoc> searchBySession(Long sessionId, Long userId) {
        try {
            ObjectNode bool = objectMapper.createObjectNode();
            ArrayNode filter = bool.putObject("bool").putArray("filter");
            filter.addObject().putObject("term").put("session_id", sessionId);
            if (userId != null) {
                filter.addObject().putObject("term").put("user_id", userId);
            }
            return search(bool, 1000);
        } catch (Exception e) {
            log.error("查询语音分析详情失败, sessionId={}, userId={}", sessionId, userId, e);
            return new ArrayList<>();
        }
    }

    private List<SpeechAnalysisDoc> searchByUser(Long userId, long startMillis) {
        try {
            ObjectNode bool = objectMapper.createObjectNode();
            ArrayNode filter = bool.putObject("bool").putArray("filter");
            filter.addObject().putObject("term").put("user_id", userId);
            filter.addObject().putObject("range").putObject("timestamp").put("gte", startMillis);
            return search(bool, 1000);
        } catch (Exception e) {
            log.error("查询语音聚合统计失败, userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /** 执行搜索并解析 _source 列表。 */
    private List<SpeechAnalysisDoc> search(JsonNode query, int size) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.set("query", query);
        body.put("size", size);

        Request request = new Request("POST", "/" + SPEECH_ANALYTICS_INDEX + "/_search");
        request.setJsonEntity(objectMapper.writeValueAsString(body));
        Response response = restClient.performRequest(request);
        int status = response.getStatusLine().getStatusCode();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("查询 ES 失败, status=" + status);
        }
        String respBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(respBody);
        List<SpeechAnalysisDoc> docs = new ArrayList<>();
        JsonNode hits = root.path("hits").path("hits");
        if (hits.isArray()) {
            for (JsonNode hit : hits) {
                JsonNode source = hit.path("_source");
                if (source.isObject()) {
                    docs.add(objectMapper.treeToValue(source, SpeechAnalysisDoc.class));
                }
            }
        }
        return docs;
    }

    private double avg(List<SpeechAnalysisDoc> docs, java.util.function.Function<SpeechAnalysisDoc, Double> getter) {
        return docs.stream()
                .map(getter)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double avgPauseMillis(List<SpeechAnalysisDoc> docs) {
        return docs.stream()
                .filter(d -> d.getPauses() != null && !d.getPauses().isEmpty())
                .flatMap(d -> d.getPauses().stream())
                .filter(p -> p.getDurationSec() != null)
                .mapToDouble(p -> p.getDurationSec() * 1000.0)
                .average()
                .orElse(0.0);
    }

    private long periodMillis(String period) {
        if (period == null || period.isBlank()) {
            return 30L * 24 * 3600 * 1000;
        }
        String p = period.trim().toLowerCase();
        try {
            if (p.endsWith("d")) {
                return Long.parseLong(p.substring(0, p.length() - 1)) * 24L * 3600 * 1000;
            }
            if (p.endsWith("m")) {
                return Long.parseLong(p.substring(0, p.length() - 1)) * 30L * 24 * 3600 * 1000;
            }
            if (p.endsWith("y")) {
                return Long.parseLong(p.substring(0, p.length() - 1)) * 365L * 24 * 3600 * 1000;
            }
        } catch (NumberFormatException ignored) {
        }
        return 30L * 24 * 3600 * 1000;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private double round0(double v) {
        return Math.round(v);
    }
}
