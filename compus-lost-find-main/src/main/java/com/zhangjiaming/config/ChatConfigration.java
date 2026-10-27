package com.zhangjiaming.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * ChatConfigration
 *
 * 智能客服服务：基于 RAG（检索增强生成）回答用户问题。
 * 流程：向量检索知识库 -> 拼接上下文 -> 调用 DeepSeek 生成回答。
 */
@Configuration
public class ChatConfigration {



    /** 客服系统提示词 */
    private static final String SYSTEM_PROMPT =
            "你是校园失物招领平台的智能客服助手，请用友好、简洁、准确的中文回答用户问题。";
            //+ "回答要基于提供的【知识库内容】；若知识库中没有相关信息，请礼貌说明并建议用户查看平台公告或联系管理员。";

    @Bean
    public ChatClient ChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())

                .build();
    }
}