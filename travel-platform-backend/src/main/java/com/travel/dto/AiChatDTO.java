package com.travel.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 问答请求
 */
@Data
public class AiChatDTO {

    /** 用户输入的问题 */
    private String message;

    /** 多轮对话历史（可选），role: user/assistant */
    private List<History> history;

    @Data
    public static class History {
        /** user / assistant */
        private String role;
        private String content;
    }
}
