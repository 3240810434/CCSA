package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.PropertyFeeBill;
import java.util.List;

@Dao
public interface PropertyFeeBillDao {
    @Insert
    void insert(PropertyFeeBill... bills);

    @Update
    void update(PropertyFeeBill... bills);

    @Delete
    void delete(PropertyFeeBill... bills);

    @Query("SELECT * FROM property_fee_bill WHERE phone = :phone ORDER BY createTime DESC")
    List<PropertyFeeBill> getByPhone(String phone);

    // 新增：按手机号查询并按周期结束时间倒序
    @Query("SELECT * FROM property_fee_bill WHERE phone = :phone ORDER BY periodEnd DESC")
    List<PropertyFeeBill> getByPhoneOrderByPeriodDesc(String phone);

    @Query("SELECT * FROM property_fee_bill WHERE community = :community AND status = 0")
    List<PropertyFeeBill> getUnpaidByCommunity(String community);

    @Query("UPDATE property_fee_bill SET status = :status WHERE id IN (:ids)")
    void updateStatusByIds(int status, List<Long> ids);

    // 在 PropertyFeeBillDao 接口中添加
    @Query("SELECT * FROM property_fee_bill WHERE community = :community")
    List<PropertyFeeBill> getByCommunity(String community);


}