package com.rashik.rashikmart.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {

    private int id;
    private int sellerId;

    private String name;
    private String description;
    private String category;

    private BigDecimal price;
    private int quantity;
    private String imageUrl = "default-product.svg";
    private Timestamp createdAt;

    public Product() {
    }

    public Product(
            int sellerId,
            String name,
            String description,
            String category,
            BigDecimal price,
            int quantity
    ) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = "default-product.svg";
    }

    public Product(
            int sellerId,
            String name,
            String description,
            String category,
            BigDecimal price,
            int quantity,
            String imageUrl
    ) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : "default-product.svg";
    }

    public Product(
            int id,
            int sellerId,
            String name,
            String description,
            String category,
            BigDecimal price,
            int quantity
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = "default-product.svg";
    }

    public Product(
            int id,
            int sellerId,
            String name,
            String description,
            String category,
            BigDecimal price,
            int quantity,
            String imageUrl
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : "default-product.svg";
    }

    public Product(
            int id,
            int sellerId,
            String name,
            String description,
            String category,
            BigDecimal price,
            int quantity,
            String imageUrl,
            Timestamp createdAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : "default-product.svg";
        this.createdAt = createdAt;
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Alias methods for stock
    public int getStock() {
        return quantity;
    }

    public void setStock(int stock) {
        this.quantity = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : "default-product.svg";
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}