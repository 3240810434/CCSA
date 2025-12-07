package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.VoteRecord;

@Dao
public interface VoteRecordDao {
    @Insert
    void insert(VoteRecord record);

    @Query("SELECT * FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    VoteRecord getVoteRecord(long voteId, String userId);

    @Query("SELECT COUNT(*) FROM vote_records WHERE voteId = :voteId AND isAgree = 1")
    int getAgreeCount(long voteId);

    @Query("SELECT COUNT(*) FROM vote_records WHERE voteId = :voteId AND isAgree = 0")
    int getOpposeCount(long voteId);
}