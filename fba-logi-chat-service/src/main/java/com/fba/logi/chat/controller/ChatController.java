package com.fba.logi.chat.controller;

import com.fba.logi.agent.core.AgentExecutor;
import com.fba.logi.agent.core.AgentState;
import com.fba.logi.api.request.ChatRequest;
import com.fba.logi.api.response.ChatResponse;
import com.fba.logi.api.dto.ChatMessageDTO;
import com.fba.logi.common.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "聊天接口", description = "AI Agent 对话接口")
public class ChatController {

    private final AgentExecutor agentExecutor;
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    /**
     * 营销策略官对话
     */
    @PostMapping("/marketing/strategist")
    @Operation(summary = "营销策略官对话", description = "与营销策略官 Agent 进行对话")
    public Response<ChatResponse> chatWithStrategist(@Valid @RequestBody ChatRequest request) {
        return chat(request, "marketing_strategist");
    }

    /**
     * 销售助手对话
     */
    @PostMapping("/marketing/sales")
    @Operation(summary = "销售助手对话", description = "与销售助手 Agent 进行对话")
    public Response<ChatResponse> chatWithSales(@Valid @RequestBody ChatRequest request) {
        return chat(request, "marketing_sales");
    }

    /**
     * 订舱助手对话
     */
    @PostMapping("/order/booking")
    @Operation(summary = "订舱助手对话", description = "与订舱助手 Agent 进行对话")
    public Response<ChatResponse> chatWithBooking(@Valid @RequestBody ChatRequest request) {
        return chat(request, "order_booking");
    }

    /**
     * 仓库主管对话
     */
    @PostMapping("/warehouse/ops")
    @Operation(summary = "仓库主管对话", description = "与仓库主管 Agent 进行对话")
    public Response<ChatResponse> chatWithOps(@Valid @RequestBody ChatRequest request) {
        return chat(request, "warehouse_ops");
    }

    /**
     * 通用对话接口
     */
    @PostMapping
    @Operation(summary = "通用对话", description = "根据 agentType 路由到对应 Agent")
    public Response<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return chat(request, request.getAgentType());
    }

    /**
     * 流式对话接口
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话", description = "使用 SSE 进行流式对话")
    public SseEmitter streamChat(
            @RequestParam String message,
            @RequestParam String agentType,
            @RequestParam(required = false) String sessionId) {

        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        String finalSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        streamExecutor.execute(() -> {
            try {
                // 构建 Agent 状态
                AgentState state = AgentState.builder()
                        .sessionId(finalSessionId)
                        .agentType(agentType)
                        .userMessage(message)
                        .build();

                // 流式执行 Agent
                agentExecutor.executeStream(state, new com.fba.logi.infrastructure.adapter.llm.ILlmClient.StreamResponseHandler() {
                    @Override
                    public void onToken(String token) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(token));
                        } catch (Exception e) {
                            log.error("SSE 发送失败", e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data("{\"sessionId\":\"" + finalSessionId + "\"}"));
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("SSE 完成失败", e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(throwable.getMessage()));
                            emitter.completeWithError(throwable);
                        } catch (Exception e) {
                            log.error("SSE 错误处理失败", e);
                        }
                    }
                });

            } catch (Exception e) {
                log.error("流式对话执行失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 执行对话
     */
    private Response<ChatResponse> chat(ChatRequest request, String agentType) {
        log.info("收到对话请求，Agent: {}，消息: {}", agentType, request.getMessage());

        // 构建 Agent 状态
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        AgentState state = AgentState.builder()
                .sessionId(sessionId)
                .agentType(agentType)
                .userMessage(request.getMessage())
                .build();

        // 执行 Agent
        AgentState result = agentExecutor.execute(state);

        // 构建响应
        ChatResponse response = ChatResponse.builder()
                .sessionId(sessionId)
                .assistantMessage(ChatMessageDTO.builder()
                        .messageId(UUID.randomUUID().toString())
                        .sessionId(sessionId)
                        .role("assistant")
                        .content(result.getFinalResponse())
                        .agentType(agentType)
                        .createTime(LocalDateTime.now())
                        .build())
                .build();

        return Response.success(response);
    }

}
