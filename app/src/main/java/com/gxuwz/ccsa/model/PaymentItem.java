package com.gxuwz.ccsa.model;

import java.io.Serializable;

public class PaymentItem implements Serializable {
    private String building;
    private String room;
    private String owner;
    private double receivable;
    private double paid;
    private String status;
    private String date;
    private String period; // 缴费周期（年月）
    private String phone;

    public PaymentItem(String building, String room, String owner, double receivable,
                       double paid, String status, String date, String period, String phone) {
        this.building = building;
        this.room = room;
        this.owner = owner;
        this.receivable = receivable;
        this.paid = paid;
        this.status = status;
        this.date = date;
        this.period = period;
        this.phone = phone;
    }

    // Getter 和 Setter 方法
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public double getReceivable() { return receivable; }
    public void setReceivable(double receivable) { this.receivable = receivable; }

    public double getPaid() { return paid; }
    public void setPaid(double paid) { this.paid = paid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }


    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}