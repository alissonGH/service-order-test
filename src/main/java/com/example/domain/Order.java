package com.example.domain;

import java.math.BigDecimal;
import java.util.List;

public class Order {

    private Long id;
    private String externalId;
    private List<Product> products;
    private BigDecimal totalValue;
    private String status;

    public Order() {}

    public Order(Long id, String externalId, List<Product> products, BigDecimal totalValue, String status) {
        this.id = id;
        this.externalId = externalId;
        this.products = products;
        this.totalValue = totalValue;
        this.status = status;
    }

    public Order(String externalId, List<Product> products) {
        this.externalId = externalId;
        this.products = products;
        this.status = String.valueOf(OrderStatus.PENDING);
    }

    public void calculateTotal() {
        boolean hasInvalidProduct = products.stream()
                .anyMatch(p -> p.getPrice().compareTo(BigDecimal.ZERO) < 0 || p.getQuantity() < 0);

        if (hasInvalidProduct) {
            throw new IllegalArgumentException("Products cannot have negative price or quantity.");
        }

        this.totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    public Long getId() { return id; }
    public String getExternalId() { return externalId; }
    public List<Product> getProducts() { return products; }
    public BigDecimal getTotalValue() { return totalValue; }
    public String getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public void setProducts(List<Product> products) { this.products = products; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    public void setStatus(String status) { this.status = status; }
}
