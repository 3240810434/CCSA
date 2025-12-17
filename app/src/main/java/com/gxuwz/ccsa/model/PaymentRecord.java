package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "payment_record")
public class PaymentRecord implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String community;
    private String building;
    private String roomNumber;
    private String phone;
    private double amount;
    private String period;
    private int status; // 1-已缴
    private long payTime;
    private String receiptNumber;

    // 【新增】费用明细快照 (JSON格式: {"service":100, "elevator":20...})
    private String feeDetailsSnapshot;

    // 构造方法
    public PaymentRecord(String community, String building, String roomNumber, String phone,
                         double amount, String period, int status, long payTime,
                         String receiptNumber, String feeDetailsSnapshot) {
        this.community = community;
        this.building = building;
        this.roomNumber = roomNumber;
        this.phone = phone;
        this.amount = amount;
        this.period = period;
        this.status = status;
        this.payTime = payTime;
        this.receiptNumber = receiptNumber;
        this.feeDetailsSnapshot = feeDetailsSnapshot;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public long getPayTime() { return payTime; }
    public void setPayTime(long payTime) { this.payTime = payTime; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    // 【新增】Getter/Setter
    public String getFeeDetailsSnapshot() { return feeDetailsSnapshot; }
    public void setFeeDetailsSnapshot(String feeDetailsSnapshot) { this.feeDetailsSnapshot = feeDetailsSnapshot; }
}