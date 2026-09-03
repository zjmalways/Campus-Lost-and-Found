package com.zhangjiaming.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能客服服务：基于 RAG（检索增强生成）回答用户问题。
 * 流程：向量检索知识库 -> 拼接上下文 -> 调用 DeepSeek 生成回答。
 */
@Service
public class ChatService {

    /** 客服系统提示词 */
    private static final String SYSTEM_PROMPT =
            "你是校园失物招领平台的智能客服助手，请用友好、简洁、准确的中文回答用户问题。"
            + "回答要基于提供的【知识库内容】；若知识库中没有相关信息，请礼貌说明并建议用户查看平台公告或联系管理员。";

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.vectorStore = vectorStore;
    }

    /**
     * 根据用户问题检索知识库并生成回答
     *
     * @param question 用户问题
     * @return 客服回答
     */
    public String chat(String question) {
        // 1. 向量检索最相关的知识库片段
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(4).build());

        // 2. 拼接上下文
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        if (context.isBlank()) {
            context = "暂无相关知识库内容。";
        }

        // 3. 调用 DeepSeek 大模型生成回答
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("【知识库内容】\n" + context + "\n\n【用户问题】\n" + question)
                .call()
                .content();
    }
}
