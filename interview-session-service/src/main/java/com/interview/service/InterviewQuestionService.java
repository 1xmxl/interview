package com.interview.service;

import com.interview.DTO.ParsedResumeData;
import com.interview.DTO.StructuredResume;
import com.interview.domain.po.InterviewConfigs;
import com.interview.service.SessionContextService.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 出题服务：基于面试配置 + 候选人简历 + 对话历史，由 LLM 生成面试题。
 * system prompt 硬编码默认值，后续可在 Nacos 覆盖。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionService {

    private final ChatClient.Builder chatClientBuilder;

    /** 生成第一题 */
    public String generateFirstQuestion(InterviewConfigs config, ParsedResumeData resume, int totalQuestions) {
        String system = buildSystemPrompt(config, resume);
        String user = "请出第 1 道面试题（本次面试共 " + totalQuestions + " 道）。只输出题目本身，不要任何解释或序号。";
        return call(system, user);
    }

    /** 生成下一题（携带完整对话历史） */
    public String generateNextQuestion(InterviewConfigs config, ParsedResumeData resume,
                                       List<ChatMessage> history, int nextIndex) {
        String system = buildSystemPrompt(config, resume);
        String historyText = formatHistory(history);
        String user = "以下是此前面试的对话记录：\n" + historyText +
                "\n请根据以上对话，出第 " + nextIndex + " 道面试题（可针对候选人回答中的薄弱点追问，或考察新的知识点）。只输出题目本身，不要任何解释或序号。";
        return call(system, user);
    }

    private String call(String system, String user) {
        return chatClientBuilder.build().prompt()
                .system(system)
                .user(user)
                .call()
                .content();
    }

    private String buildSystemPrompt(InterviewConfigs config, ParsedResumeData resume) {
        String style = styleText(config.getInterviewerStyle());
        boolean en = "EN".equalsIgnoreCase(config.getLanguage());
        String language = en ? "英文" : "中文";
        String difficulty = difficultyText(config.getDifficulty());
        String skills = config.getTargetSkills() == null || config.getTargetSkills().isEmpty()
                ? "不限" : String.join("、", config.getTargetSkills());
        String categories = config.getQuestionCategories() == null || config.getQuestionCategories().isEmpty()
                ? "综合" : String.join("、", config.getQuestionCategories());
        String resumeText = buildResumeText(resume);

        return "你是一名" + style + "的专业技术面试官，正在主持一场" + language + "技术面试。\n" +
                "面试配置：难度 " + difficulty + "，重点考察技能：" + skills + "，题目类别：" + categories + "。\n" +
                (resumeText.isBlank() ? "" : "候选人简历背景：\n" + resumeText + "\n") +
                "出题要求：紧扣岗位技能与候选人简历，由浅入深，不重复已问过的点；" +
                "若候选人回答暴露薄弱点，可针对性追问。" +
                (en ? "请用英文提问。" : "");
    }

    /** 简历结构化数据 → 可读文本摘要 */
    private String buildResumeText(ParsedResumeData resume) {
        if (resume == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            sb.append("技能：").append(resume.getSkills().stream()
                    .map(StructuredResume.SkillEntry::getSkillName)
                    .collect(Collectors.joining("、"))).append("\n");
        }
        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            sb.append("工作经历：").append(resume.getExperiences().stream()
                    .map(e -> (e.getCompany() == null ? "" : e.getCompany()) +
                            (e.getPosition() == null ? "" : " " + e.getPosition()))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("；"))).append("\n");
        }
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            sb.append("项目经历：").append(resume.getProjects().stream()
                    .map(p -> (p.getProjectName() == null ? "" : p.getProjectName()) +
                            (p.getRole() == null ? "" : "（" + p.getRole() + "）"))
                    .collect(Collectors.joining("；"))).append("\n");
        }
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            sb.append("教育背景：").append(resume.getEducations().stream()
                    .map(ed -> (ed.getInstitution() == null ? "" : ed.getInstitution()) +
                            (ed.getDegree() == null ? "" : " " + ed.getDegree()) +
                            (ed.getFieldOfStudy() == null ? "" : " " + ed.getFieldOfStudy()))
                    .collect(Collectors.joining("；"))).append("\n");
        }
        return sb.toString();
    }

    private String formatHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return "（暂无历史对话）";
        }
        return history.stream()
                .map(m -> ("assistant".equals(m.getRole()) ? "面试官：" : "候选人：") + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private String styleText(String style) {
        if (style == null) {
            return "专业严谨";
        }
        return switch (style.toUpperCase()) {
            case "FRIENDLY" -> "亲切友好";
            case "PRESSURE" -> "高压追问";
            case "STRICT" -> "严谨严格";
            default -> "专业严谨";
        };
    }

    private String difficultyText(String difficulty) {
        if (difficulty == null) {
            return "中等";
        }
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "简单";
            case "HARD" -> "困难";
            default -> "中等";
        };
    }
}
