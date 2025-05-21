package com.example.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void shouldCalculateTotalCorrectly() {
        Product p1 = new Product(1L, "Product A", new BigDecimal("10.00"), 2);
        Product p2 = new Product(2L, "Product B", new BigDecimal("5.50"), 3);

        Order order = new Order("external-123", List.of(p1, p2));

        order.calculateTotal();

        assertEquals(new BigDecimal("36.50"), order.getTotalValue());
    }

    @Test
    void shouldThrowExceptionWhenProductPriceIsNegative() {
        Product p1 = new Product(1L, "Invalid Product", new BigDecimal("-10.00"), 1);
        Order order = new Order("external-124", List.of(p1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, order::calculateTotal);
        assertEquals("Products cannot have negative price or quantity.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProductQuantityIsNegative() {
        Product p1 = new Product(1L, "Invalid Product", new BigDecimal("10.00"), -1);
        Order order = new Order("external-125", List.of(p1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, order::calculateTotal);
        assertEquals("Products cannot have negative price or quantity.", exception.getMessage());
    }

    @Test
    void shouldUpdateStatus() {
        Product p1 = new Product(1L, "Product A", new BigDecimal("10.00"), 1);
        Order order = new Order("external-126", List.of(p1));

        order.updateStatus("PROCESSED");

        assertEquals("PROCESSED", order.getStatus());
    }

    @Test
    void shouldSetAndGetAttributes() {
        Product p1 = new Product(1L, "Product A", new BigDecimal("10.00"), 1);
        Order order = new Order(1L, "external-127", List.of(p1), new BigDecimal("10.00"), "PENDING");

        assertEquals(1L, order.getId());
        assertEquals("external-127", order.getExternalId());
        assertEquals(1, order.getProducts().size());
        assertEquals(new BigDecimal("10.00"), order.getTotalValue());
        assertEquals("PENDING", order.getStatus());
    }
}
