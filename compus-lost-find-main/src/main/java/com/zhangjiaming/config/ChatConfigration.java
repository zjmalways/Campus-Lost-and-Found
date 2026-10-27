package com.zhangjiaming.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * ChatConfigration
 *
 * 智能客服配置：调用 DeepSeek，并提供失物招领 Function Calling 工具。
 */
@Configuration
public class ChatConfigration {



    /** 客服系统提示词 */
    private static final String SYSTEM_PROMPT =
            """
            你是校园失物招领平台的智能客服助手，请用友好、简洁、准确的中文回答用户问题。
            当问题涉及平台物品或公告的实时信息时，优先使用提供的查询工具，不要编造结果。
            仅在用户明确提出发布、修改或删除时调用对应写入工具；删除操作必须先向用户确认目标记录，获得明确确认后才能执行。
            不要尝试伪造用户身份、发布者信息或管理员权限；工具会按当前登录用户和现有权限校验操作。
            """;
            //+ "回答要基于提供的【知识库内容】；若知识库中没有相关信息，请礼貌说明并建议用户查看平台公告或联系管理员。";

    @Bean
    public ChatClient ChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}