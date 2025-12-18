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

    @Query("SELECT * FROM orders WHERE id = :id")
    Order getOrderById(long id);

    // 居民查询自己的订单
    @Query("SELECT * FROM orders WHERE residentId = :residentId ORDER BY id DESC")
    List<Order> getOrdersByResident(String residentId);

    // 商家查询待接单列表
    @Query("SELECT * FROM orders WHERE merchantId = :merchantId AND status = '待接单' ORDER BY id DESC")
    List<Order> getPendingOrdersByMerchant(String merchantId);

    // 商家查询特定状态的订单 (屏蔽售后中的订单)
    @Query("SELECT * FROM orders WHERE merchantId = :merchantId AND status = :status AND afterSalesStatus = 0 ORDER BY id DESC")
    List<Order> getOrdersByMerchantAndStatus(String merchantId, String status);

    // 更新售后状态
    @Query("UPDATE orders SET afterSalesStatus = :status WHERE id = :orderId")
    void updateAfterSalesStatus(Long orderId, int status);

    // 商家查询所有售后相关的订单
    @Query("SELECT * FROM orders WHERE merchantId = :merchantId AND afterSalesStatus > 0 ORDER BY afterSalesStatus ASC, id DESC")
    List<Order> getMerchantAfterSalesOrders(String merchantId);

    // --- 新增：更新评价状态 ---
    @Query("UPDATE orders SET isReviewed = :status WHERE id = :orderId")
    void updateReviewStatus(long orderId, int status);
}