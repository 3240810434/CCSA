package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.VoteRecord;
import java.util.List;

@Dao
public interface VoteRecordDao {
    // 对应 VoteDetailActivity 中的 db.voteDao().insertRecord(record)
    @Insert
    void insertRecord(VoteRecord record);

    // 对应 VoteDetailActivity 中的 db.voteDao().getVoteRecord(vote.getId(), userId)
    @Query("SELECT * FROM vote_records WHERE voteId = :voteId AND userId = :userId")
    VoteRecord getVoteRecord(long voteId, String userId);

    // 对应 VoteDetailActivity 中的 db.voteDao().getAllRecordsForVote(vote.getId())
    // 移除了无效的 getAgreeCount/getOpposeCount，改用获取所有记录后在内存中统计
    @Query("SELECT * FROM vote_records WHERE voteId = :voteId")
    List<VoteRecord> getAllRecordsForVote(long voteId);

}