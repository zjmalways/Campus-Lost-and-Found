package com.zhangjiaming.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import com.zhangjiaming.entity.ChatMessage;
import com.zhangjiaming.mapper.ItemMapper;
import com.zhangjiaming.service.AnnouncementService;
import com.zhangjiaming.service.ChatHistoryService;
import com.zhangjiaming.service.ChatService;
import com.zhangjiaming.service.ItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 执行客服对话，注入可用工具，并异步持久化对话消息。
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final AnnouncementService announcementService;
    private final ChatHistoryService chatHistoryService;

    /** 执行不带会话历史的普通问答调用。 */
    @Override
    public String chatCall(String question) {

        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /** 加载历史消息、调用模型及工具，并记录流式回答的最终状态。 */
    @Override
    public Flux<String> chatStream(
            String question, Long sessionId, Long userId, String username, Integer userRole) {
        List<ChatMessage> history = chatHistoryService.loadContext(userId, sessionId);
        List<Message> messages = history.stream()
                .map(message -> "ASSISTANT".equals(message.getRole())
                        ? (Message) new AssistantMessage(message.getContent())
                        : new UserMessage(message.getContent()))
                .collect(Collectors.toCollection(ArrayList::new));
        messages.add(new UserMessage(question));
        chatHistoryService.saveMessage(userId, sessionId, "USER", question, "COMPLETED");

        long startedAt = System.nanoTime();
        StringBuilder answer = new StringBuilder();
        log.debug("Starting chat completion userId={} sessionId={} contextMessages={}",
                userId, sessionId, history.size());

        return chatClient.prompt()
                .messages(messages)
                .tools(new ChatFunctionTools(itemService, itemMapper, announcementService,
                        userId, username, userRole))
                .stream()
                .content()
                .doOnNext(answer::append)
                .concatWith(Flux.defer(() -> {
                    chatHistoryService.saveMessage(
                            userId, sessionId, "ASSISTANT", answer.toString(), "COMPLETED");
                    log.debug("Completed chat completion userId={} sessionId={} responseChars={} durationMs={}",
                            userId, sessionId, answer.length(),
                            (System.nanoTime() - startedAt) / 1_000_000);
                    return Flux.empty();
                }))
                .doOnCancel(() -> {
                    chatHistoryService.saveMessage(
                            userId, sessionId, "ASSISTANT", answer.toString(), "CANCELLED");
                    log.debug("Cancelled chat completion userId={} sessionId={} responseChars={} durationMs={}",
                            userId, sessionId, answer.length(),
                            (System.nanoTime() - startedAt) / 1_000_000);
                })
                .onErrorResume(error -> {
                    chatHistoryService.saveMessage(
                            userId, sessionId, "ASSISTANT", answer.toString(), "FAILED");
                    log.debug("Failed chat completion userId={} sessionId={} errorType={} durationMs={}",
                            userId, sessionId, error.getClass().getSimpleName(),
                            (System.nanoTime() - startedAt) / 1_000_000);
                    return Flux.error(error);
                });
    }
}
