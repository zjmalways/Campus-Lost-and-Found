package com.zhangjiaming.dto;

import lombok.Data;

/**
 * 创建客服会话的请求参数。
 */
@Data
public class CreateChatSessionRequest {

    /** 可选会话标题；未提供时由服务端按首条消息生成。 */
    private String title;
}
