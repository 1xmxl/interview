package com.interview.service;

import com.interview.DTO.ResumeDeleteEvent;
import com.interview.DTO.StructuredResume;
import com.interview.constant.RabbitMQConstant;
import com.interview.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;


import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {
    private final VectorStore vectorStore;
    public void addRAGData(Long resumeId, StructuredResume aiReturnInfo,Long userId) {
        if (userId == null) {
            // 根据业务逻辑处理，例如抛出异常、使用默认值或记录错误并返回
            throw new IllegalStateException("User ID cannot be null when adding RAG data.");
            // 或者使用一个默认/占位符值
            // userId = "anonymous";
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId.toString());
        map.put("resumeId", resumeId);
        String buildMainContent = buildMainContent(aiReturnInfo);
        Document document = Document.builder().text(buildMainContent)
                .id(resumeId.toString())
                .metadata(map).build();
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        vectorStore.add(documents);
    }
    private String buildMainContent(StructuredResume resume) {
        StringBuilder sb = new StringBuilder();
        if (resume.getSkills() != null) {
            sb.append("技能: ").append(resume.getSkills().stream()
                    .map(StructuredResume.SkillEntry::getSkillName).collect(Collectors.joining(", "))).append("; ");
        }
        if (resume.getExperiences() != null) {
            sb.append("经历: ").append(resume.getExperiences().stream()
                    .map(e -> e.getCompany() + e.getPosition()).collect(Collectors.joining(", "))).append("; ");
        }
        if (resume.getProjects() != null) {
            sb.append("项目: ").append(resume.getProjects().stream()
                    .map(StructuredResume.ProjectEntry::getProjectName).collect(Collectors.joining(", "))).append("; ");
        }
        // 可补充 rawText 前200字
        return sb.toString();
    }
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(RabbitMQConstant.Queue.RESUME_DELETE_QUEUE),
                    exchange = @Exchange(RabbitMQConstant.Exchange.RESUME_EXCHANGE),
                    key = RabbitMQConstant.Key.RESUME_DELETE_KEY
            )
    )
    public void RAGDataDelete(ResumeDeleteEvent resumeDeleteEvent) {
        Long resumeId = resumeDeleteEvent.getResumeId();
        vectorStore.delete(resumeId.toString());
    }

}
