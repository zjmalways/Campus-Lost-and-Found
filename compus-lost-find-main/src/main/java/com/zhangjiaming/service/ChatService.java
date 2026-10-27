package com.zhangjiaming.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    public String chatCall(String question);
    public Flux<String> chatStream(
            String question, Long sessionId, Long userId, String username, Integer userRole);
}