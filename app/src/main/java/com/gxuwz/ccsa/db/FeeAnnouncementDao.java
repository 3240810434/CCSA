package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.FeeAnnouncement;
import java.util.List;

@Dao
public interface FeeAnnouncementDao {
    @Insert
    void insert(FeeAnnouncement... announcements);

    @Update
    void update(FeeAnnouncement... announcements);

    @Delete
    void delete(FeeAnnouncement... announcements);

    @Query("SELECT * FROM fee_announcement WHERE community = :community ORDER BY publishTime DESC")
    List<FeeAnnouncement> getByCommunity(String community);

    @Query("SELECT * FROM fee_announcement WHERE id = :id")
    FeeAnnouncement getById(long id);
}

