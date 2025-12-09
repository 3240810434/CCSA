package com.gxuwz.ccsa.model;

import java.util.Date;

/**
 * 统一消息模型：用于在消息列表中同时展示系统通知和聊天记录
 */
public class UnifiedMessage implements Comparable<UnifiedMessage> {
    public static final int TYPE_SYSTEM_NOTICE = 0; // 系统通知
    public static final int TYPE_CHAT_MESSAGE = 1;  // 聊天消息

    private int type;           // 消息类型
    private String title;       // 标题（系统通知标题 或 聊天对象名字）
    private String content;     // 内容
    private long time;          // 时间戳
    private Object data;        // 原始数据对象（Notification 或 ChatMessage）

    // 聊天特有字段
    private int chatTargetId;   // 聊天对象的ID（用于跳转）
    private String avatarUrl;   // 头像（可选）

    // 构造函数：用于系统通知
    public UnifiedMessage(Notification notification) {
        this.type = TYPE_SYSTEM_NOTICE;
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.time = notification.getCreateTime().getTime();
        this.data = notification;
    }

    // 构造函数：用于聊天消息
    public UnifiedMessage(ChatMessage chatMessage, String targetName, int targetId, String avatarUrl) {
        this.type = TYPE_CHAT_MESSAGE;
        this.title = targetName; // 聊天显示对方名字
        this.content = chatMessage.content;
        this.time = chatMessage.createTime;
        this.data = chatMessage;
        this.chatTargetId = targetId;
        this.avatarUrl = avatarUrl;
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTime() { return time; }
    public Object getData() { return data; }
    public int getChatTargetId() { return chatTargetId; }
    public String getAvatarUrl() { return avatarUrl; }

    // 倒序排列（最新的在前面）
    @Override
    public int compareTo(UnifiedMessage o) {
        return Long.compare(o.time, this.time);
    }
}