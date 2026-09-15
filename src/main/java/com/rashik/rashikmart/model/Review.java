package com.rashik.rashikmart.model;

import java.sql.Timestamp;

public class Review {

    private int id;
    private int productId;
    private int buyerId;
    private int rating;
    private String reviewText;
    private Timestamp createdAt;

    // Populated via JOIN with users for display
    private String buyerName;
    private String buyerEmail;

    public Review() {
    }

    public Review(
            int productId,
            int buyerId,
            int rating,
            String reviewText
    ) {
        this.productId = productId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public Review(
            int id,
            int productId,
            int buyerId,
            int rating,
            String reviewText,
            Timestamp createdAt
    ) {
        this.id = id;
        this.productId = productId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    // =========================
    // ID
    // =========================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // =========================
    // PRODUCT ID
    // =========================

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    // =========================
    // BUYER ID
    // =========================

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    // =========================
    // RATING
    // =========================

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    // =========================
    // REVIEW TEXT
    // =========================

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    // =========================
    // CREATED AT
    // =========================

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // =========================
    // BUYER NAME (JOIN)
    // =========================

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    // =========================
    // BUYER EMAIL (JOIN)
    // =========================

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }
}