package com.fba.logi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 消息角色：user / assistant / system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * Agent 类型
     */
    private String agentType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
