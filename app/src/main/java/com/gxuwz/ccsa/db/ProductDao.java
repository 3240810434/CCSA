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
    @Query("SELECT * FROM product ORDER BY createTime DESC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE merchantId = :merchantId ORDER BY createTime DESC")
    List<Product> getProductsByMerchant(int merchantId);

    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}