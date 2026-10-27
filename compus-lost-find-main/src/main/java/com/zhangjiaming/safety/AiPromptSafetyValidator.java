package com.zhangjiaming.safety;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 对 AI 用户输入执行长度、控制字符及明显提示攻击模式检查。
 */
@Component
public class AiPromptSafetyValidator {

    public static final int MAX_PROMPT_LENGTH = 4_000;

    private static final Pattern ENGLISH_INSTRUCTION_OVERRIDE = Pattern.compile(
            "\\b(ignore|disregard|override|bypass)\\b.{0,60}\\b"
                    + "(previous|prior|above|system|developer|safety)\\b.{0,40}\\b"
                    + "(instructions?|prompts?|rules?|polic(?:y|ies))\\b");
    private static final Pattern ENGLISH_PROMPT_EXTRACTION = Pattern.compile(
            "\\b(reveal|show|print|repeat|leak|expose)\\b.{0,50}\\b"
                    + "(system|developer|hidden|internal)\\b.{0,30}\\b"
                    + "(prompt|instructions?|rules?)\\b");
    private static final List<Pattern> CHINESE_PROMPT_ATTACKS = List.of(
            Pattern.compile("(忽略|无视|忘记|覆盖|绕过).{0,12}(之前|以上|所有|系统|开发者|安全).{0,12}(指令|提示词|规则|限制|政策)"),
            Pattern.compile("(泄露|输出|显示|复述|打印|告诉我).{0,12}(系统|开发者|隐藏|内部).{0,12}(提示词|指令|规则|内容)")
    );

    /**
     * 校验用户问题并返回去除首尾空白后的文本。
     * 规则属于启发式检查，不能替代模型侧或工具调用侧的安全控制。
     */
    public String validate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new AiPromptRejectedException("问题不能为空");
        }
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            throw new AiPromptRejectedException(
                    "问题长度不能超过" + MAX_PROMPT_LENGTH + "个字符");
        }
        if (prompt.codePoints().anyMatch(codePoint ->
                Character.isISOControl(codePoint)
                        && codePoint != '\n'
                        && codePoint != '\r'
                        && codePoint != '\t')) {
            throw new AiPromptRejectedException("问题包含不支持的控制字符");
        }

        String normalized = Normalizer.normalize(prompt, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .strip();
        if (ENGLISH_INSTRUCTION_OVERRIDE.matcher(normalized).find()
                || ENGLISH_PROMPT_EXTRACTION.matcher(normalized).find()
                || CHINESE_PROMPT_ATTACKS.stream().anyMatch(pattern -> pattern.matcher(normalized).find())) {
            throw new AiPromptRejectedException("问题包含不允许的提示词覆盖或系统提示词提取指令");
        }
        return prompt.strip();
    }
}
