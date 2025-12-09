package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.ChatMessage;
import java.util.List;

@Dao
public interface ChatDao {
    @Insert
    void insertMessage(ChatMessage message);

    // 获取两个用户之间的聊天记录
    @Query("SELECT * FROM chat_message WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY createTime ASC")
    List<ChatMessage> getChatHistory(int userId1, int userId2);

    // 获取与我有过聊天的所有消息（用于在业务层筛选最近会话）
    @Query("SELECT * FROM chat_message WHERE senderId = :myId OR receiverId = :myId ORDER BY createTime DESC")
    List<ChatMessage> getAllMyMessages(int myId);

    // 删除与某人的所有聊天记录
    @Query("DELETE FROM chat_message WHERE (senderId = :myId AND receiverId = :targetId) OR (senderId = :targetId AND receiverId = :myId)")
    void deleteConversation(int myId, int targetId);
}
