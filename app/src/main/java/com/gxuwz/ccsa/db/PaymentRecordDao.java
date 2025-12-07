package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.PaymentRecord;
import java.util.List;

@Dao
public interface PaymentRecordDao {
    // 插入记录并返回ID
    @Insert
    long insert(PaymentRecord record);

    @Update
    void update(PaymentRecord... records);

    @Delete
    void delete(PaymentRecord... records);

    // 修正：根据手机号查询缴费记录，排序字段改为payTime（与实体类字段一致）
    @Query("SELECT * FROM payment_record WHERE phone = :phone ORDER BY payTime DESC")
    List<PaymentRecord> getByPhone(String phone);

    @Query("SELECT SUM(amount) FROM payment_record WHERE community = :community AND status = 1")
    double getTotalPaidAmount(String community);

    @Query("SELECT COUNT(*) FROM payment_record WHERE community = :community AND status = 0")
    int getUnpaidCount(String community);
}