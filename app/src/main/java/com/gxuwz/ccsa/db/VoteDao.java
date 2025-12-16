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
    @Insert
    long insert(Vote vote);

    @Update
    void update(Vote vote);

    @Delete
    void delete(Vote vote);

    // 获取特定状态的投票（草稿或已发布）
    @Query("SELECT * FROM votes WHERE community = :community AND status = :status ORDER BY publishTime DESC")
    List<Vote> getVotesByStatus(String community, int status);

    // 获取所有已发布的投票（给居民看）
    @Query("SELECT * FROM votes WHERE community = :community AND status = 1 ORDER BY publishTime DESC")
    List<Vote> getPublishedVotes(String community);

    // 记录相关
    @Insert
    void insertRecord(VoteRecord record);

    @Query("SELECT * FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    VoteRecord getVoteRecord(long voteId, String userId);

    @Query("SELECT * FROM vote_records WHERE voteId = :voteId")
    List<VoteRecord> getAllRecordsForVote(long voteId);

    // 删除该投票的所有记录
    @Query("DELETE FROM vote_records WHERE voteId = :voteId")
    void deleteAllRecords(long voteId);
}