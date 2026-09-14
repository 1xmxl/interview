package com.interview.aiTools;

import client.ResumeClient;

import com.interview.DTO.ParsedResumeData;
import com.interview.DTO.StructuredResume;

import com.interview.config.SystemPromptConfig;
import com.interview.constant.RabbitMQConstant;
import com.interview.constant.ResumeParseMessage;
import com.interview.context.UserContext;

import com.interview.service.RAGService;
import com.interview.util.OssUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResumeParseConsumer {
    @Autowired
    @Qualifier("noMemoryChatClient")
    private ChatClient chatClient;
    private final OssUtil ossUtil;
    private final Parser parser = new AutoDetectParser();
    public final ResumeClient resumeClient;
    private final ParseContext parseContext = new ParseContext();
    private final SystemPromptConfig systemPromptConfig;
    private final RAGService ragService;
    private final VectorStore vectorStore;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(RabbitMQConstant.Queue.resume_parse_queue),
                    exchange = @Exchange(RabbitMQConstant.Exchange.RESUME_EXCHANGE),
                    key = RabbitMQConstant.Key.RESUME_PARSE_LEY
            )
    )
    public void resumeParse(ResumeParseMessage message) throws IOException, SAXException, TikaException {
        Long userId = message.getUserId();
        Long resumeId = message.getResumeId();
        String objectName = message.getObjectName();
        InputStream fileStream = null;
//        if(fileStream == null){
//            log.info("not find fileStream");
//            return;
//        }
        try {
            fileStream = ossUtil.getObject(objectName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String content = extractPlainText(fileStream, objectName);
        StructuredResume aiReturnInfo = chatClient.prompt().system(
                a -> {
                    a.text(systemPromptConfig.getContentGetMessage().get());
                }
        ).user(content).call().entity(StructuredResume.class);
        List<StructuredResume.EducationEntry> educations = aiReturnInfo.getEducations();
        List<StructuredResume.ExperienceEntry> experiences = aiReturnInfo.getExperiences();
        List<StructuredResume.ProjectEntry> projects = aiReturnInfo.getProjects();
        List<StructuredResume.SkillEntry> skills = aiReturnInfo.getSkills();
        ParsedResumeData dbmsData = ParsedResumeData.builder().skills(skills)
                .projects(projects)
                .educations(educations)
                .experiences(experiences)
                .build();
        ragService.addRAGData(resumeId, aiReturnInfo,userId);
        resumeClient.updateStatus(resumeId, userId, dbmsData);
        log.info("简历解析并保存成功，resumeId={}", resumeId);
    }



    private String extractPlainText(InputStream inputStream, String fileName) throws TikaException, IOException, SAXException {
        Metadata metadata = new Metadata();
        metadata.set("resourceName", fileName);
        BodyContentHandler bodyContentHandler = new BodyContentHandler(-1);
        parser.parse(inputStream,bodyContentHandler,metadata,parseContext);
        return bodyContentHandler.toString().trim();
    }

}
