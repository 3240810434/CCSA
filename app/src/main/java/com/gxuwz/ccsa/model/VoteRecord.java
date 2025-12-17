package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// 【修复 1】添加 indices 参数，建立 voteId 和 userId 的联合唯一索引 (unique = true)
// 这样数据库会拒绝插入重复的 (voteId, userId) 组合
@Entity(tableName = "vote_records", indices = {@Index(value = {"voteId", "userId"}, unique = true)})
public class VoteRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long voteId;
    private String userId;
    private String selectedIndices; // 用户选择的选项索引，如 "0,2" 代表选了第1和第3个

    public VoteRecord(long voteId, String userId, String selectedIndices) {
        this.voteId = voteId;
        this.userId = userId;
        this.selectedIndices = selectedIndices;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getVoteId() { return voteId; }
    public void setVoteId(long voteId) { this.voteId = voteId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSelectedIndices() { return selectedIndices; }
    public void setSelectedIndices(String selectedIndices) { this.selectedIndices = selectedIndices; }
}