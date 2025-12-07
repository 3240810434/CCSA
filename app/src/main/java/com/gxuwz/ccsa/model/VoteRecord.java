// CCSA/app/src/main/java/com/gxuwz/ccsa/model/VoteRecord.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vote_records")
public class VoteRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long voteId; // 投票ID
    private String userId; // 用户ID(手机号)
    private boolean isAgree; // true:赞成, false:反对

    public VoteRecord(long voteId, String userId, boolean isAgree) {
        this.voteId = voteId;
        this.userId = userId;
        this.isAgree = isAgree;
    }

    // getter和setter方法
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getVoteId() { return voteId; }
    public void setVoteId(long voteId) { this.voteId = voteId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isAgree() { return isAgree; }
    public void setAgree(boolean agree) { isAgree = agree; }
}
