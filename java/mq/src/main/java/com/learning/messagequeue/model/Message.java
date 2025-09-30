package com.learning.messagequeue.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息模型类
 * 
 * 用于在消息队列中传递的结构化数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 重试次数
     */
    private int retryCount;
    
    /**
     * 发送者
     */
    private String sender;
    
    /**
     * 优先级（用于流式优先级消息）
     */
    private int priority;
    
    /**
     * 窗口ID（用于流式窗口化消息）
     */
    private long windowId;
    
    /**
     * 组ID（用于流式聚合消息）
     */
    private int groupId;
    
    /**
     * 是否应该包含（用于流式过滤消息）
     */
    private boolean shouldInclude;
    
    /**
     * 创建消息
     */
    public static Message create(String content, String type, String sender) {
        return new Message(
            java.util.UUID.randomUUID().toString(),
            content,
            type,
            LocalDateTime.now(),
            0,
            sender
        );
    }
}
