package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.Repair;

import java.util.List;

@Dao
public interface RepairDao {
    @Insert
    void insert(Repair repair);

    @Update
    void update(Repair repair);

    @Query("SELECT * FROM repairs WHERE userId = :userId ORDER BY submitTime DESC")
    List<Repair> getByUserId(String userId);

    @Query("SELECT * FROM repairs WHERE id = :id")
    Repair getById(long id);

    @Query("SELECT * FROM repairs WHERE repairNo = :repairNo")
    Repair getByRepairNo(String repairNo);

    @Query("SELECT * FROM repairs ORDER BY submitTime DESC")
    List<Repair> getAll();

    @Query("SELECT COUNT(*) FROM repairs WHERE status = 0")
    int getPendingCount();

    @Query("SELECT * FROM repairs WHERE status = 0 ORDER BY submitTime ASC")
    List<Repair> getPendingRepairs();

    // 修复：表名从 repair 改为 repairs
    @Query("SELECT * FROM repairs WHERE community = :community ORDER BY submitTime DESC")
    List<Repair> getByCommunity(String community);

    // 修复：表名从 repair 改为 repairs
    @Query("SELECT COUNT(*) FROM repairs WHERE status = 0 AND community = :community")
    int getPendingCountByCommunity(String community);

    // 获取指定小区的待受理报修
    @Query("SELECT * FROM repairs WHERE community = :community AND status = 0 ORDER BY submitTime DESC")
    List<Repair> getPendingByCommunity(String community);

    // 获取所有待受理报修
    @Query("SELECT * FROM repairs WHERE status = 0 ORDER BY submitTime DESC")
    List<Repair> getAllPending();

    // 获取指定小区的已完成报修
    @Query("SELECT * FROM repairs WHERE community = :community AND status = 1 ORDER BY completeTime DESC")
    List<Repair> getCompletedByCommunity(String community);

    // 获取所有已完成报修
    @Query("SELECT * FROM repairs WHERE status = 1 ORDER BY completeTime DESC")
    List<Repair> getAllCompleted();
}
