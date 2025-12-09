package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.gxuwz.ccsa.model.ChatMessage;
import java.util.List;

@Dao
public interface ChatDao {
    @Insert
    void insertMessage(ChatMessage message);

    // 【修复】获取聊天记录：只显示我没有删除的消息
    // 逻辑：
    // 1. 如果我是发送者(senderId = :myId)，且 isDeletedBySender 为 0 (false)
    // 2. 或者我是接收者(receiverId = :myId)，且 isDeletedByReceiver 为 0 (false)
    // 同时对方要是 targetId
    @Query("SELECT * FROM chat_message " +
            "WHERE ((" +
            "   (senderId = :myId AND receiverId = :targetId AND isDeletedBySender = 0) " +
            "   OR " +
            "   (senderId = :targetId AND receiverId = :myId AND isDeletedByReceiver = 0)" +
            ")) " +
            "ORDER BY createTime ASC")
    List<ChatMessage> getChatHistory(int myId, int targetId);

    // 【修复】获取所有与我有关且未被我删除的消息
    @Query("SELECT * FROM chat_message " +
            "WHERE (senderId = :myId AND isDeletedBySender = 0) " +
            "OR (receiverId = :myId AND isDeletedByReceiver = 0) " +
            "ORDER BY createTime DESC")
    List<ChatMessage> getAllMyMessages(int myId);

    // 【新增】标记发送方删除了消息
    @Query("UPDATE chat_message SET isDeletedBySender = 1 WHERE senderId = :myId AND receiverId = :targetId")
    void markDeletedBySender(int myId, int targetId);

    // 【新增】标记接收方删除了消息
    @Query("UPDATE chat_message SET isDeletedByReceiver = 1 WHERE receiverId = :myId AND senderId = :targetId")
    void markDeletedByReceiver(int myId, int targetId);

    // 【修复】逻辑删除会话（事务操作）
    // 不需要真的 DELETE，而是分别更新我作为发送者和我作为接收者的所有消息状态
    @Transaction
    default void deleteConversation(int myId, int targetId) {
        markDeletedBySender(myId, targetId);   // 把我发给对方的标记为我已删
        markDeletedByReceiver(myId, targetId); // 把对方发给我的标记为我已删
    }

    // 可选：真正物理删除（只有当双方都标记删除了才真的删掉，节省空间，通常由定时任务处理，此处可忽略）
}