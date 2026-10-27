package com.zhangjiaming.safety;

/**
 * 表示用户输入未通过 AI 提示词安全校验。
 */
public class AiPromptRejectedException extends RuntimeException {

    /** 使用校验拒绝原因创建异常。 */
    public AiPromptRejectedException(String message) {
        super(message);
    }
}
