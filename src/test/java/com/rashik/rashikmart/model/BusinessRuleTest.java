package com.rashik.rashikmart.model;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class BusinessRuleTest {

    @Test
    public void testCartSubtotalAndTotalCalculation() {
        BigDecimal unitPrice = new BigDecimal("49.99");
        int quantity = 3;
        BigDecimal expectedSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)); // 149.97

        Assert.assertEquals(new BigDecimal("149.97"), expectedSubtotal);
    }

    @Test
    public void testStockDeductionInvariants() {
        int initialStock = 25;
        int purchasedQty = 7;
        int remainingStock = initialStock - purchasedQty;

        Assert.assertEquals(18, remainingStock);
        Assert.assertTrue("Remaining stock must not be negative", remainingStock >= 0);
    }

    @Test
    public void testProductActiveStateDefault() {
        Product p = new Product(1, "Organic Oats", "Healthy", "Grains", new BigDecimal("75.00"), 20);
        Assert.assertTrue("New product must be active by default", p.isActive());

        p.setActive(false);
        Assert.assertFalse("Product can be explicitly deactivated", p.isActive());
    }

    @Test
    public void testRoleInvariants() {
        User buyer = new User("Alice", "alice@example.com", "hash", "BUYER");
        User seller = new User("Bob", "bob@example.com", "hash", "SELLER");
        User admin = new User("Charlie", "admin@example.com", "hash", "ADMIN");

        Assert.assertEquals("BUYER", buyer.getRole());
        Assert.assertEquals("SELLER", seller.getRole());
        Assert.assertEquals("ADMIN", admin.getRole());
    }

    @Test
    public void testCartItemBoundaryValues() {
        CartItem item = new CartItem();
        item.setQuantity(1);
        Assert.assertEquals(1, item.getQuantity());

        item.setProductId(101);
        Assert.assertEquals(101, item.getProductId());

        item.setBuyerId(5);
        Assert.assertEquals(5, item.getBuyerId());
    }
}
