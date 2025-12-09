package com.gxuwz.ccsa.model;

import com.gxuwz.ccsa.model.Notification;
import com.gxuwz.ccsa.model.ChatMessage;

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
    private Object data;        // 原始数据对象 (Notification 或 ChatMessage)

    // 聊天特有字段
    private int chatTargetId;   // 聊天对象的ID（对方的ID）
    private String avatarUrl;   // 对方头像

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
        // 如果名字为空，给一个默认显示，防止出现“未知用户”带来的困惑，实际显示时会根据数据库最新数据
        this.title = (targetName == null || targetName.isEmpty()) ? "邻居 " + targetId : targetName;
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

    // 按时间倒序排列（最新的在前面）
    @Override
    public int compareTo(UnifiedMessage o) {
        return Long.compare(o.time, this.time);
    }
}