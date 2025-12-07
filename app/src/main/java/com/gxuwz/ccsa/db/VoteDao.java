// CCSA/app/src/main/java/com/gxuwz/ccsa/db/VoteDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.Vote;
import com.gxuwz.ccsa.model.VoteRecord;

import java.util.List;

@Dao
public interface VoteDao {
    // 投票相关操作
    @Insert
    void insert(Vote vote);

    @Update
    void update(Vote vote);

    @Delete
    void delete(Vote vote);

    @Query("SELECT * FROM votes WHERE community = :community ORDER BY publishTime DESC")
    List<Vote> getVotesByCommunity(String community);

    @Query("SELECT * FROM votes WHERE id = :id")
    Vote getVoteById(long id);

    // 投票记录相关操作
    @Insert
    void insertRecord(VoteRecord record);

    @Query("SELECT * FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    VoteRecord getVoteRecord(long voteId, String userId);

    @Query("SELECT COUNT(*) FROM vote_records WHERE voteId = :voteId AND isAgree = 1")
    int getAgreeCount(long voteId);

    @Query("SELECT COUNT(*) FROM vote_records WHERE voteId = :voteId AND isAgree = 0")
    int getOpposeCount(long voteId);
}