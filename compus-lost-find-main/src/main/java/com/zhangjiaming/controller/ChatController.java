package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.dto.ChatRequest;
import com.zhangjiaming.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "智能客服", description = "基于 RAG 知识库的问答客服")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 智能问答：检索知识库 + 调用大模型生成回答
     */
    @Operation(summary = "智能问答", description = "基于 RAG 知识库检索增强生成，回答用户问题")
    @PostMapping
    public Result<String> chat(@Valid @RequestBody ChatRequest request) {
        try {
            String answer = chatService.chat(request.getQuestion());
            return Result.success(answer);
        } catch (Exception e) {
            log.error("智能客服问答失败", e);
            return Result.error(ErrorContext.CHAT_ERROR);
        }
    }
}
