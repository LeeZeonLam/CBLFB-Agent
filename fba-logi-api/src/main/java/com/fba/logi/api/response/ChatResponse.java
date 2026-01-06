package com.fba.logi.api.response;

import com.fba.logi.api.dto.ChatMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 助手回复消息
     */
    private ChatMessageDTO assistantMessage;

    /**
     * 工具调用结果（如有）
     */
    private List<ToolCallResult> toolResults;

    /**
     * 工具调用结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallResult {
        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 执行结果
         */
        private Object result;

        /**
         * 是否成功
         */
        private Boolean success;
    }

}
