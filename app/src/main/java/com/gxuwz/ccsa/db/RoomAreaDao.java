package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.RoomArea;

import java.util.List;

@Dao
public interface RoomAreaDao {
    @Query("SELECT * FROM room_areas WHERE community = :community AND building = :building ORDER BY roomNumber")
    List<RoomArea> getByCommunityAndBuilding(String community, String building);

    @Query("SELECT DISTINCT building FROM room_areas WHERE community = :community ORDER BY building")
    List<String> getBuildingsByCommunity(String community);

    // 新增：按小区、楼栋、房号查询
    @Query("SELECT * FROM room_areas WHERE community = :community AND building = :building AND roomNumber = :roomNumber LIMIT 1")
    RoomArea getByCommunityBuildingAndRoom(String community, String building, String roomNumber);

    @Insert
    void insert(RoomArea... roomAreas);

    @Update
    void update(RoomArea... roomAreas);

    @Delete
    void delete(RoomArea roomArea);

    @Query("DELETE FROM room_areas WHERE community = :community AND building = :building")
    void deleteByCommunityAndBuilding(String community, String building);

    @Query("SELECT * FROM room_areas WHERE community = :community ORDER BY building, roomNumber")
    List<RoomArea> getByCommunity(String community);
}