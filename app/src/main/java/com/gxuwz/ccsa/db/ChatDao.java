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

    // 【修改】获取聊天记录：同时匹配 ID 和 Role
    // 逻辑：(我是发送者 且 我角色对 且 对方是接收者 且 对方角色对) 或 (反之)
    @Query("SELECT * FROM chat_message " +
            "WHERE ((" +
            "   (senderId = :myId AND senderRole = :myRole AND receiverId = :targetId AND receiverRole = :targetRole AND isDeletedBySender = 0) " +
            "   OR " +
            "   (senderId = :targetId AND senderRole = :targetRole AND receiverId = :myId AND receiverRole = :myRole AND isDeletedByReceiver = 0)" +
            ")) " +
            "ORDER BY createTime ASC")
    List<ChatMessage> getChatHistory(int myId, String myRole, int targetId, String targetRole);

    // 【修改】获取所有与我有关的消息
    @Query("SELECT * FROM chat_message " +
            "WHERE (senderId = :myId AND senderRole = :myRole AND isDeletedBySender = 0) " +
            "OR (receiverId = :myId AND receiverRole = :myRole AND isDeletedByReceiver = 0) " +
            "ORDER BY createTime DESC")
    List<ChatMessage> getAllMyMessages(int myId, String myRole);

    // 标记发送方删除
    @Query("UPDATE chat_message SET isDeletedBySender = 1 WHERE senderId = :myId AND senderRole = :myRole AND receiverId = :targetId AND receiverRole = :targetRole")
    void markDeletedBySender(int myId, String myRole, int targetId, String targetRole);

    // 标记接收方删除
    @Query("UPDATE chat_message SET isDeletedByReceiver = 1 WHERE receiverId = :myId AND receiverRole = :myRole AND senderId = :targetId AND senderRole = :targetRole")
    void markDeletedByReceiver(int myId, String myRole, int targetId, String targetRole);

    // 删除会话
    @Transaction
    default void deleteConversation(int myId, String myRole, int targetId, String targetRole) {
        markDeletedBySender(myId, myRole, targetId, targetRole);
        markDeletedByReceiver(myId, myRole, targetId, targetRole);
    }
}