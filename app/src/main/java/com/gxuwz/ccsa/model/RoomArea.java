package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "room_areas")
public class RoomArea {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String community;
    private String building;
    private String roomNumber;
    private double area;
    private int floor;

    // 主要构造函数（Room将使用此构造函数，必须包含所有非自增字段）
    public RoomArea(String community, String building, String roomNumber, double area, int floor) {
        this.community = community;
        this.building = building;
        this.roomNumber = roomNumber;
        this.area = area;
        this.floor = floor;
    }

    // 次要构造函数（必须添加@Ignore让Room忽略）
    @Ignore
    public RoomArea(String community, String building, String roomNumber, double area) {
        this.community = community;
        this.building = building;
        this.roomNumber = roomNumber;
        this.area = area;
        this.floor = 1; // 默认楼层
    }

    // 所有字段的Getter和Setter（必须完整）
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
}