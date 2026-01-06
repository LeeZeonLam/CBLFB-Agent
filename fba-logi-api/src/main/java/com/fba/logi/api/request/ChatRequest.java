package com.fba.logi.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 会话 ID（可选，不传则新建会话）
     */
    private String sessionId;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * Agent 类型
     */
    @NotBlank(message = "Agent 类型不能为空")
    private String agentType;

    /**
     * 是否开启流式输出
     */
    private Boolean stream;

}
