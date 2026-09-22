package com.zhangjiaming.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.SearchRequest;
//import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能客服服务：基于 RAG（检索增强生成）回答用户问题。
 * 流程：向量检索知识库 -> 拼接上下文 -> 调用 DeepSeek 生成回答。
 */
@Slf4j
@Service
public class ChatService {

    /** 客服系统提示词 */
    private static final String SYSTEM_PROMPT =
            "你是校园失物招领平台的智能客服助手，请用友好、简洁、准确的中文回答用户问题。";
            //+ "回答要基于提供的【知识库内容】；若知识库中没有相关信息，请礼貌说明并建议用户查看平台公告或联系管理员。";

    private final ChatClient chatClient;
    //private final VectorStore vectorStore;

    public ChatService(@Qualifier("ollamaChatModel") ChatModel chatModel) {//, VectorStore vectorStore
        this.chatClient = ChatClient.builder(chatModel).build();
        //this.vectorStore = vectorStore;
    }

    /**
     * 根据用户问题检索知识库并生成回答
     *
     * @param question 用户问题
     * @return 客服回答
     */
    public String chat(String question) {
        // 1. 向量检索最相关的知识库片段
        //List<Document> documents = vectorStore.similaritySearch(
        //        SearchRequest.builder().query(question).topK(4).build());

        // 2. 拼接上下文
//        String context = documents.stream()
//                .map(Document::getText)
//                .collect(Collectors.joining("\n\n"));
//        if (context.isBlank()) {
//            context = "暂无相关知识库内容。";
//        }

        // 3. 调用 DeepSeek 大模型生成回答
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("\n\n【用户问题】\n" + question)//【知识库内容】\n" + context +
                .call()
                .content();
    }

//    public Flux<String> streamRagChat(String userQuestion) {
//        log.info("收到流式问答请求：{}", userQuestion);
//        List<Document> similarDocs = vectorStore.similaritySearch(userQuestion);
//        StringBuilder context = new StringBuilder();
//        similarDocs.forEach(doc -> context.append(doc.getContent()).append("\n"));
//
//        String promptTemplate = """
//                你是校园失物招领智能客服，严格基于下面知识库内容回答用户问题。
//                如果知识库没有相关信息，直接说明无法回答，不要编造信息。
//                知识库：
//                {context}
//                用户问题：{question}
//                """;
//
//        // stream() 而不是 call()
//        return chatClient.prompt()
//                .user(promptTemplate)
//                .param("context", context.toString())
//                .param("question", userQuestion)
//                .stream()
//                .content();
//    }

    public Flux<String> streamChat(String userQuestion) {
        log.info("收到用户问题：{}", userQuestion);

        String systemPrompt = """
                你是校园失物招领智能客服，回答简洁友好。
                """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userQuestion)
                .stream()
                .content();
    }

}
