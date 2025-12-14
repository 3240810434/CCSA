package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.gxuwz.ccsa.model.AfterSalesRecord;

@Dao
public interface AfterSalesRecordDao {
    @Insert
    void insert(AfterSalesRecord record);

    @Update
    void update(AfterSalesRecord record);

    @Query("SELECT * FROM after_sales_records WHERE order_id = :orderId LIMIT 1")
    AfterSalesRecord getRecordByOrderId(Long orderId);
}