package com.zhangjiaming.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.zhangjiaming.service.ChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor 
@Service 
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    @Override 
    public String chatCall(String question) {

        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @Override 
    public Flux<String> chatStream(String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }
}
