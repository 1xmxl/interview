package com.interview.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class SpringAIConfig {
    @Value("${tj.ai.memory.max:100}")
    private Integer maxMessages;
//    @Autowired
//    private RedisChatMemoryRepository redisChatMemoryRepository;
    /**
     * 配置 ChatClient
     */
//    @Bean
//    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
//                                 Advisor loggerAdvisor,
//                                 Advisor messageChatMemoryAdvisor,
//                                 Advisor messageOptimizeAdvisor) {  // 日志记录器
//        return chatClientBuilder
//                .defaultAdvisors(loggerAdvisor,messageOptimizeAdvisor,messageChatMemoryAdvisor)//添加 Advisor 功能增强
//                .defaultTools()
//                .build();
//    }
    @Bean
    public ChatClient noMemoryChatClient(ChatClient.Builder chatClientBuilder,
                                 Advisor loggerAdvisor) {  // 日志记录器
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor)//添加 Advisor 功能增强
                .defaultTools()
                .build();
    }
    /**
     * 日志记录器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }
//    @Bean
//    public RedisChatMemoryRepository redisChatMemoryRepository(StringRedisTemplate stringRedisTemplate) {
//        return new RedisChatMemoryRepository(stringRedisTemplate); // 使用默认 prefix
//        // 或者：return new RedisChatMemoryRepository(stringRedisTemplate, "MY_PREFIX:");
//    }
//    @Bean
//    public ChatMemory redisChatMemory(ChatMemoryRepository chatMemoryRepository) {
//        return MessageWindowChatMemory
//                .builder().chatMemoryRepository(chatMemoryRepository)
//                .maxMessages(maxMessages)
//                .build();
//    }
//    @Bean
//    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory)
//    {
//        return MessageChatMemoryAdvisor.builder(chatMemory).build();
//    }
//    @Bean
//    public Advisor messageOptimizeAdvisor(RedisChatMemoryRepository redisChatMemoryRepository) {
//        return new MessageOptimizeAdvisor(redisChatMemoryRepository);
//    }
}
