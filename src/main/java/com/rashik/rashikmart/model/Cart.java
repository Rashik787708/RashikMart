package com.rashik.rashikmart.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private int id;
    private int buyerId;
    private Timestamp createdAt;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public Cart(int id, int buyerId, Timestamp createdAt) {
        this.id = id;
        this.buyerId = buyerId;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (CartItem item : items) {
                total = total.add(item.getSubtotal());
            }
        }
        return total;
    }

    public int getTotalQuantity() {
        int total = 0;
        if (items != null) {
            for (CartItem item : items) {
                total += item.getQuantity();
            }
        }
        return total;
    }
}
