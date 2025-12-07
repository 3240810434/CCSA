package com.gxuwz.ccsa.model;

import java.util.List;

public class FilterData {
    private List<String> selectedYears;
    private List<String> selectedMonths;
    private List<String> selectedBuildings;
    private String paymentStatus;

    // Getter 和 Setter 方法
    public List<String> getSelectedYears() { return selectedYears; }
    public void setSelectedYears(List<String> selectedYears) { this.selectedYears = selectedYears; }

    public List<String> getSelectedMonths() { return selectedMonths; }
    public void setSelectedMonths(List<String> selectedMonths) { this.selectedMonths = selectedMonths; }

    public List<String> getSelectedBuildings() { return selectedBuildings; }
    public void setSelectedBuildings(List<String> selectedBuildings) { this.selectedBuildings = selectedBuildings; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}