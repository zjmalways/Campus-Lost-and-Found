package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.dto.CreateChatSessionRequest;
import com.zhangjiaming.entity.ChatMessage;
import com.zhangjiaming.entity.ChatSession;
import com.zhangjiaming.safety.AiPromptRejectedException;
import com.zhangjiaming.safety.AiPromptSafetyValidator;
import com.zhangjiaming.service.ChatHistoryService;
import com.zhangjiaming.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 提供客服会话管理、历史查询和安全校验后的流式对话接口。
 */
@RequiredArgsConstructor 
@Slf4j
@Tag(name = "智能客服", description = "支持 Function Calling 和持久化会话历史的流式问答")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatHistoryService chatHistoryService;
    private final AiPromptSafetyValidator promptSafetyValidator;

    /** 创建当前登录用户的客服会话。 */
    @Operation(summary = "创建客服会话")
    @PostMapping("/sessions")
    public Result<ChatSession> createSession(
            HttpServletRequest request, @RequestBody(required = false) CreateChatSessionRequest body) {
        Long userId = requireUserId(request);
        ChatSession session = chatHistoryService.createSession(
                userId, body == null ? null : body.getTitle());
        log.debug("Created chat session userId={} sessionId={}", userId, session.getSessionId());
        return Result.success(session);
    }

    /** 查询当前登录用户的会话列表。 */
    @Operation(summary = "查询当前用户的客服会话")
    @GetMapping("/sessions")
    public Result<List<ChatSession>> listSessions(HttpServletRequest request) {
        return Result.success(chatHistoryService.listSessions(requireUserId(request)));
    }

    /** 查询指定会话的消息历史，并由服务层校验会话归属。 */
    @Operation(summary = "查询客服会话消息")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> listMessages(
            HttpServletRequest request, @PathVariable Long sessionId) {
        return Result.success(chatHistoryService.listMessages(requireUserId(request), sessionId));
    }

    /** 校验输入与会话归属后，启动带有历史上下文的流式对话。 */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "智能客服流式问答")
    public Flux<String> streamChat(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(name = "X-Chat-Session-Id", required = false) Long requestedSessionId,
            @RequestBody String question) {
        Long userId = requireUserId(request);
        String validatedQuestion;
        try {
            validatedQuestion = promptSafetyValidator.validate(question);
        } catch (AiPromptRejectedException exception) {
            log.debug("Rejected unsafe AI prompt userId={} reason={}", userId, exception.getMessage());
            throw exception;
        }
        String username = (String) request.getAttribute("username");
        Integer userRole = (Integer) request.getAttribute("role");
        Long sessionId = requestedSessionId;
        if (sessionId == null) {
            sessionId = chatHistoryService.createSession(userId, null).getSessionId();
        } else {
            chatHistoryService.assertSessionOwner(userId, sessionId);
        }
        response.setHeader("X-Chat-Session-Id", sessionId.toString());
        log.debug("Accepted chat stream userId={} sessionId={}", userId, sessionId);
        return chatService.chatStream(validatedQuestion, sessionId, userId, username, userRole);
    }

    private static Long requireUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("请先登录后再访问客服会话");
        }
        return userId;
    }

}
