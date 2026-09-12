package com.rashik.rashikmart.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SellerOrderItem {

    private int orderId;
    private Timestamp orderDate;
    private String orderStatus;
    private String buyerName;
    private String buyerEmail;
    private int productId;
    private String productName;
    private String productCategory;
    private String productImage;
    private int quantity;
    private BigDecimal price;

    public SellerOrderItem() {
    }

    public SellerOrderItem(
            int orderId,
            Timestamp orderDate,
            String orderStatus,
            String buyerName,
            String buyerEmail,
            int productId,
            String productName,
            String productCategory,
            String productImage,
            int quantity,
            BigDecimal price
    ) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productImage = productImage;
        this.quantity = quantity;
        this.price = price;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        if (price != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
