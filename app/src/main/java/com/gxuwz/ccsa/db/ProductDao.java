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

    @Query("SELECT * FROM product")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE merchantId = :merchantId ORDER BY id DESC")
    List<Product> getProductsByMerchantId(int merchantId);

    @Query("SELECT * FROM product WHERE id = :productId")
    Product getProductById(int productId);
}