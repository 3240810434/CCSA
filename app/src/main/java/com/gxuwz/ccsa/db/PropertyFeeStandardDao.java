package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.PropertyFeeStandard;
import java.util.List;

@Dao
public interface PropertyFeeStandardDao {

    @Insert
    void insert(PropertyFeeStandard... standards);


    @Insert
    long insert(PropertyFeeStandard standard);

    @Update
    void update(PropertyFeeStandard... standards);

    @Delete
    void delete(PropertyFeeStandard... standards);

    @Query("SELECT * FROM property_fee_standard WHERE community = :community ORDER BY updateTime DESC")
    List<PropertyFeeStandard> getByCommunity(String community);

    @Query("SELECT * FROM property_fee_standard WHERE id = :id")
    PropertyFeeStandard getById(long id);

    @Query("SELECT * FROM property_fee_standard WHERE community = :community ORDER BY updateTime DESC LIMIT 1")
    PropertyFeeStandard getLatestByCommunity(String community);


}

