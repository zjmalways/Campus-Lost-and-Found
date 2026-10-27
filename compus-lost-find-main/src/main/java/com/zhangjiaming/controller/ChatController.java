package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor 
@Slf4j
@Tag(name = "智能客服", description = "基于 RAG 知识库的问答客服")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    /**
     * 智能问答：检索知识库 + 调用大模型生成回答
     */
    @Operation(summary = "智能问答", description = "基于 RAG 知识库检索增强生成，回答用户问题")

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(HttpServletRequest request, @RequestBody String question) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("流式问答，当前登录userId：{}", userId);
        return chatService.chatStream(question);
    }

}
