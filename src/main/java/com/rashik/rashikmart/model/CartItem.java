package com.rashik.rashikmart.model;

import java.math.BigDecimal;

public class CartItem {

    private int buyerId;
    private int productId;
    private int quantity;
    private Product product;

    public CartItem() {
    }

    public CartItem(
            int buyerId,
            int productId,
            int quantity,
            Product product
    ) {
        this.buyerId = buyerId;
        this.productId = productId;
        this.quantity = quantity;
        this.product = product;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getSubtotal() {

        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }

        return product.getPrice()
                .multiply(BigDecimal.valueOf(quantity));
    }
}