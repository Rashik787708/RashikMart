package com.rashik.rashikmart.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private int buyerId;
    private BigDecimal totalAmount;
    private String status = "CONFIRMED";
    private Timestamp createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(int buyerId, BigDecimal totalAmount, String status) {
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Order(int id, int buyerId, BigDecimal totalAmount, String status, Timestamp createdAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public int getTotalQuantity() {
        int count = 0;
        if (items != null) {
            for (OrderItem item : items) {
                count += item.getQuantity();
            }
        }
        return count;
    }
}
