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
 * AI 面试官对话服务：像真人面试一样自然对话。
 *
 * 核心变化（相对早期"固定题数出题器"）：
 * 1. 开场白：AI 先打招呼并说明看过简历，而不是直接抛题
 * 2. 自然对话：每轮 AI 先对候选人上一轮回答做简短回应，再提问/追问
 * 3. 难度只影响「问题本身的深浅」，与对话轮数无关
 * 4. 终止由 AI 判断：认为考察得差不多时输出结束标记，系统据此收尾
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionService {

    /** AI 输出该标记表示面试可以结束了（不会展示给用户） */
    public static final String END_MARKER = "[END]";

    /** 最少问答轮数：未达到时忽略 AI 的结束标记，避免过早收尾 */
    public static final int MIN_ROUNDS = 5;

    /** 最多问答轮数：达到后强制结束，避免无限对话 */
    public static final int MAX_ROUNDS = 12;

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 生成开场白：打招呼 + 提到已看过简历 + 第一个问题，融合成一段自然口语。
     */
    public String generateOpening(InterviewConfigs config, ParsedResumeData resume, String candidateName) {
        String system = buildSystemPrompt(config, resume, candidateName);
        String user = """
                现在面试刚开始，请完成开场：
                1. 先用一句话打招呼（知道姓名就称呼姓名）；
                2. 用一句话说明你已经看过简历，点出简历中最吸引你的一两个点（没有简历信息就说想先了解技术背景）；
                3. 然后自然地提出第一个问题。
                把三部分连成一段自然口语，不要分点、不要序号、不要"面试官："前缀，控制在 150 字以内。
                """;
        return call(system, user);
    }

    /**
     * 生成下一轮回复：先回应上一轮回答，再追问或换方向；由 AI 判断是否该结束。
     *
     * @param history    完整对话历史（含候选人刚说的这句）
     * @param askedCount 已问过的问题数（仅供参考，不硬性限制）
     */
    public String generateReply(InterviewConfigs config, ParsedResumeData resume,
                                String candidateName, List<ChatMessage> history, int askedCount) {
        String system = buildSystemPrompt(config, resume, candidateName);
        String historyText = formatHistory(history);
        String user = """
                以下是到目前为止的面试对话记录：
                %s

                当前已完成 %d 轮问答。

                请作为面试官继续这场对话。要求：
                1. 先用一到两句话自然回应候选人的上一个回答（认可具体的点，或指出哪里可以更深入），不要说"很好的回答"这类空话；
                2. 然后针对刚才回答里的薄弱点、模糊处继续追问，或切换到简历/目标技能中尚未考察的方向；
                3. 整段话口语化，不要分点、不要序号、不要"面试官："前缀，控制在 200 字以内。

                【关于何时结束】
                - 只有在同时满足以下条件时才结束：① 已完成至少 5 轮问答；② 目标技能方向基本覆盖；③ 信息已足够做出评价。
                - 如果轮数还少、或还有明确没问到方向，必须继续提问，不要提前收尾。
                - **提问和收尾绝不能出现在同一段回复里**：要提问就只提问，要收尾就只收尾。
                - 结束语只做礼貌收尾（感谢参与、说明后续会有评估报告），不要再提出任何新问题，结尾不要用问号。
                - 决定结束时，在整段话最末尾单独加上标记 %s（候选人看不到该标记）；继续提问时绝对不要加这个标记。
                """.formatted(historyText, askedCount, END_MARKER);
        return call(system, user);
    }

    /** AI 的这段回复是否表示要结束面试 */
    public boolean isEnding(String reply) {
        return reply != null && reply.contains(END_MARKER);
    }

    /** 去掉结束标记，得到给用户看的干净文本 */
    public String stripEndMarker(String reply) {
        if (reply == null) {
            return "";
        }
        return reply.replace(END_MARKER, "").trim();
    }

    private String call(String system, String user) {
        try {
            String content = chatClientBuilder.build().prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .content();
            return content == null ? "" : content.trim();
        } catch (Exception e) {
            log.error("调用 LLM 失败", e);
            throw new RuntimeException("AI 面试官暂时无法响应，请稍后重试");
        }
    }

    private String buildSystemPrompt(InterviewConfigs config, ParsedResumeData resume, String candidateName) {
        String style = styleText(config.getInterviewerStyle());
        boolean en = "EN".equalsIgnoreCase(config.getLanguage());
        String language = en ? "英文" : "中文";
        String difficulty = difficultyText(config.getDifficulty());
        String skills = config.getTargetSkills() == null || config.getTargetSkills().isEmpty()
                ? "不限" : String.join("、", config.getTargetSkills());
        String categories = config.getQuestionCategories() == null || config.getQuestionCategories().isEmpty()
                ? "综合" : String.join("、", config.getQuestionCategories());
        String resumeText = buildResumeText(resume);
        String nameText = (candidateName == null || candidateName.isBlank()) ? "候选人" : candidateName;

        return """
                你是一名%s的专业技术面试官，正在和「%s」进行一场%s技术面试。
                这场面试是**自然对话**，不是考试问卷：你要听完回答再决定下一步问什么。

                【面试配置】
                - 提问深度（难度）：%s。这里的难度指**问题本身的深浅**（EASY=基础概念与用法；MEDIUM=原理与项目细节；HARD=架构取舍、边界场景、深度追问），**与对话轮数无关**。
                - 重点考察方向：%s
                - 关注题型：%s

                【候选人简历】
                %s

                【对话原则】
                1. 围绕简历和目标技能展开，从简历里的项目/经历切入最自然。
                2. 每次只问一个核心问题，可以有自然铺垫，但不要一次抛多个问题。
                3. 追问要基于候选人刚才的原话（例如他说"用了 Redis"，就追问怎么保证一致性）。
                4. 答得浅就往下追一层；答得好就换下一个方向。
                5. 不要重复已问过的点，不要机械按清单念题。
                6. 判断何时结束：关键技能点已覆盖、或信息已足够评价时礼貌收尾；但**至少进行 5 轮问答后**才允许结束。一般 5~10 轮，以你的判断为准。
                7. 结束语只做礼貌收尾（感谢、说明后续有评估报告），**不要再提问**。
                8. 全程用%s，语气%s，不要用 markdown 标题/列表符号/"问题1："这类格式。
                """.formatted(
                style, nameText, language,
                difficulty, skills, categories,
                resumeText.isBlank() ? "（候选人未上传简历，请通过提问了解其技术背景）" : resumeText,
                language, style);
    }

    /** 简历结构化数据 → 可读文本摘要 */
    private String buildResumeText(ParsedResumeData resume) {
        if (resume == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            sb.append("- 技能：").append(resume.getSkills().stream()
                    .map(StructuredResume.SkillEntry::getSkillName)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining("、"))).append("\n");
        }
        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            sb.append("- 工作/实习经历：").append(resume.getExperiences().stream()
                    .map(e -> (e.getCompany() == null ? "" : e.getCompany()) +
                            (e.getPosition() == null ? "" : " " + e.getPosition()) +
                            (e.getDescription() == null ? "" : "：" + e.getDescription()))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("；"))).append("\n");
        }
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            sb.append("- 项目经历：").append(resume.getProjects().stream()
                    .map(p -> (p.getProjectName() == null ? "" : p.getProjectName()) +
                            (p.getRole() == null ? "" : "（" + p.getRole() + "）") +
                            (p.getDescription() == null ? "" : "：" + p.getDescription()))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("；"))).append("\n");
        }
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            sb.append("- 教育背景：").append(resume.getEducations().stream()
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