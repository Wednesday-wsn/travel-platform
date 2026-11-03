package com.travel.controller.user;

import com.travel.common.result.R;
import com.travel.dto.AiChatDTO;
import com.travel.service.AiService;
import com.travel.vo.AiChatVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * 用户端：AI 问答（需登录，走 /api/user/** 鉴权）
 */
@RestController
@RequestMapping("/api/user/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /** 非流式问答（兜底） */
    @PostMapping("/chat")
    public R<AiChatVO> chat(@RequestBody AiChatDTO dto) {
        return R.ok(aiService.chat(dto.getMessage(), dto.getHistory()));
    }

    /**
     * 流式问答（SSE 打字效果）。
     * 立即返回 emitter，异步线程里逐 token 推送，避免阻塞请求线程。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatDTO dto) {
        SseEmitter emitter = new SseEmitter(120000L);
        CompletableFuture.runAsync(() -> aiService.streamChat(dto.getMessage(), dto.getHistory(), emitter));
        return emitter;
    }
}
