package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.Order;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insert(Order order);

    @Update
    void update(Order order);

    // 居民查询自己的订单
    @Query("SELECT * FROM orders WHERE residentId = :residentId ORDER BY id DESC")
    List<Order> getOrdersByResident(String residentId);

    // 商家查询待接单列表
    @Query("SELECT * FROM orders WHERE merchantId = :merchantId AND status = '待接单' ORDER BY id DESC")
    List<Order> getPendingOrdersByMerchant(String merchantId);

    // 商家查询特定状态的订单 (用于扩展处理中/已完成)
    @Query("SELECT * FROM orders WHERE merchantId = :merchantId AND status = :status ORDER BY id DESC")
    List<Order> getOrdersByMerchantAndStatus(String merchantId, String status);
}