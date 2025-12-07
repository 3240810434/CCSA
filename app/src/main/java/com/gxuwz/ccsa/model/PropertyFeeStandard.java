// CCSA/app/src/main/java/com/gxuwz/ccsa/model/PropertyFeeStandard.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "property_fee_standard")
public class PropertyFeeStandard implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String community; // 小区名称
    private double propertyServiceFeePerSquare; // 物业服务费每平方米每月费用
    private double dailyMaintenanceFund; // 日常维修资金
    private double utilityShareFeePerSquare; // 水电公摊费(每平方米)
    // 电梯费
    private int elevatorFloorStart; // 电梯费起始楼层
    private int elevatorFloorEnd; // 电梯费结束楼层
    private double elevatorFee; // 电梯费(2-几楼)
    private int elevatorFloorAbove; // 几楼及以上
    private double elevatorFeeAbove; // 电梯费(几楼及以上)
    // 高层二次加压费
    private int pressureFloorStart; // 加压费起始楼层
    private int pressureFloorEnd; // 加压费结束楼层
    private double pressureFee; // 加压费(几楼-几楼)
    private int pressureFloorAbove; // 几楼及以上
    private double pressureFeeAbove; // 加压费(几楼及以上)
    private double garbageFee; // 生活垃圾处理费
    private String effectiveDate; // 生效日期
    private String paymentCycle; // 缴费周期
    private long updateTime; // 更新时间

    public PropertyFeeStandard(String community, double propertyServiceFeePerSquare,
                               double dailyMaintenanceFund, double utilityShareFeePerSquare,
                               int elevatorFloorStart, int elevatorFloorEnd, double elevatorFee,
                               int elevatorFloorAbove, double elevatorFeeAbove,
                               int pressureFloorStart, int pressureFloorEnd, double pressureFee,
                               int pressureFloorAbove, double pressureFeeAbove,
                               double garbageFee, String effectiveDate, String paymentCycle,
                               long updateTime) {
        this.community = community;
        this.propertyServiceFeePerSquare = propertyServiceFeePerSquare;
        this.dailyMaintenanceFund = dailyMaintenanceFund;
        this.utilityShareFeePerSquare = utilityShareFeePerSquare;
        this.elevatorFloorStart = elevatorFloorStart;
        this.elevatorFloorEnd = elevatorFloorEnd;
        this.elevatorFee = elevatorFee;
        this.elevatorFloorAbove = elevatorFloorAbove;
        this.elevatorFeeAbove = elevatorFeeAbove;
        this.pressureFloorStart = pressureFloorStart;
        this.pressureFloorEnd = pressureFloorEnd;
        this.pressureFee = pressureFee;
        this.pressureFloorAbove = pressureFloorAbove;
        this.pressureFeeAbove = pressureFeeAbove;
        this.garbageFee = garbageFee;
        this.effectiveDate = effectiveDate;
        this.paymentCycle = paymentCycle;
        this.updateTime = updateTime;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
    public double getPropertyServiceFeePerSquare() { return propertyServiceFeePerSquare; }
    public void setPropertyServiceFeePerSquare(double propertyServiceFeePerSquare) { this.propertyServiceFeePerSquare = propertyServiceFeePerSquare; }
    public double getDailyMaintenanceFund() { return dailyMaintenanceFund; }
    public void setDailyMaintenanceFund(double dailyMaintenanceFund) { this.dailyMaintenanceFund = dailyMaintenanceFund; }
    public double getUtilityShareFeePerSquare() { return utilityShareFeePerSquare; }
    public void setUtilityShareFeePerSquare(double utilityShareFeePerSquare) { this.utilityShareFeePerSquare = utilityShareFeePerSquare; }
    public int getElevatorFloorStart() { return elevatorFloorStart; }
    public void setElevatorFloorStart(int elevatorFloorStart) { this.elevatorFloorStart = elevatorFloorStart; }
    public int getElevatorFloorEnd() { return elevatorFloorEnd; }
    public void setElevatorFloorEnd(int elevatorFloorEnd) { this.elevatorFloorEnd = elevatorFloorEnd; }
    public double getElevatorFee() { return elevatorFee; }
    public void setElevatorFee(double elevatorFee) { this.elevatorFee = elevatorFee; }
    public int getElevatorFloorAbove() { return elevatorFloorAbove; }
    public void setElevatorFloorAbove(int elevatorFloorAbove) { this.elevatorFloorAbove = elevatorFloorAbove; }
    public double getElevatorFeeAbove() { return elevatorFeeAbove; }
    public void setElevatorFeeAbove(double elevatorFeeAbove) { this.elevatorFeeAbove = elevatorFeeAbove; }
    public int getPressureFloorStart() { return pressureFloorStart; }
    public void setPressureFloorStart(int pressureFloorStart) { this.pressureFloorStart = pressureFloorStart; }
    public int getPressureFloorEnd() { return pressureFloorEnd; }
    public void setPressureFloorEnd(int pressureFloorEnd) { this.pressureFloorEnd = pressureFloorEnd; }
    public double getPressureFee() { return pressureFee; }
    public void setPressureFee(double pressureFee) { this.pressureFee = pressureFee; }
    public int getPressureFloorAbove() { return pressureFloorAbove; }
    public void setPressureFloorAbove(int pressureFloorAbove) { this.pressureFloorAbove = pressureFloorAbove; }
    public double getPressureFeeAbove() { return pressureFeeAbove; }
    public void setPressureFeeAbove(double pressureFeeAbove) { this.pressureFeeAbove = pressureFeeAbove; }
    public double getGarbageFee() { return garbageFee; }
    public void setGarbageFee(double garbageFee) { this.garbageFee = garbageFee; }
    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getPaymentCycle() { return paymentCycle; }
    public void setPaymentCycle(String paymentCycle) { this.paymentCycle = paymentCycle; }
    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
}