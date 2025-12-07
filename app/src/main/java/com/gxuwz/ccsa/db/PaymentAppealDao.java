package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.PaymentAppeal;

import java.util.List;

@Dao
public interface PaymentAppealDao {
    @Insert
    void insert(PaymentAppeal appeal);

    @Update
    void update(PaymentAppeal appeal);

    @Query("SELECT * FROM payment_appeal WHERE id = :id")
    PaymentAppeal getById(long id);

    @Query("SELECT * FROM payment_appeal WHERE community = :community ORDER BY submitTime DESC")
    List<PaymentAppeal> getByCommunity(String community);

    @Query("SELECT * FROM payment_appeal WHERE userId = :userId ORDER BY submitTime DESC")
    List<PaymentAppeal> getByUserId(String userId);
}
