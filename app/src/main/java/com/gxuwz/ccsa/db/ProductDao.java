package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Delete
    void delete(Product product);

    @Update
    void update(Product product);

    // 修改：只查询商家状态为开启(isOpen=1)的商品
    // 这里假设 Room 数据库中 boolean 映射为 1(true) 和 0(false)
    @Query("SELECT product.* FROM product " +
            "INNER JOIN merchant ON product.merchantId = merchant.id " +
            "WHERE merchant.isOpen = 1 " +
            "ORDER BY product.createTime DESC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE merchantId = :merchantId ORDER BY id DESC")
    List<Product> getProductsByMerchantId(int merchantId);

    @Query("SELECT * FROM product WHERE id = :productId")
    Product getProductById(int productId);

    /**
     * 根据住户所在小区筛选商品
     * 原理：内连接 Merchant 表，查找商家服务小区字段 (community) 中包含住户小区名称 (userCommunity) 的所有商品
     * 关键修改：增加 AND merchant.isOpen = 1 条件
     */
    @Query("SELECT product.* FROM product " +
            "INNER JOIN merchant ON product.merchantId = merchant.id " +
            "WHERE merchant.community LIKE '%' || :userCommunity || '%' " +
            "AND merchant.isOpen = 1 " +  // <--- 核心修改：只显示营业中商家的商品
            "ORDER BY product.createTime DESC")
    List<Product> getProductsByCommunity(String userCommunity);
}