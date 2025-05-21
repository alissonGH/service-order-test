package com.example.mapper;

import com.example.domain.Order;
import com.example.domain.Product;
import com.example.dto.OrderRequest;
import com.example.model.OrderEntity;
import com.example.model.ProductEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setExternalId(order.getExternalId());
        entity.setTotalValue(order.getTotalValue());
        entity.setStatus(Enum.valueOf(com.example.domain.OrderStatus.class, order.getStatus()));

        Set<ProductEntity> products = order.getProducts().stream()
                .map(OrderMapper::toProductEntity)
                .collect(Collectors.toSet());

        entity.setProducts(products);
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        var products = entity.getProducts().stream()
                .map(OrderMapper::toProduct)
                .collect(Collectors.toList());

        return new Order(
                entity.getId(),
                entity.getExternalId(),
                products,
                entity.getTotalValue(),
                entity.getStatus().name()
        );
    }

    private static ProductEntity toProductEntity(Product product) {
        ProductEntity pe = new ProductEntity();
        pe.setId(product.getId());
        pe.setName(product.getName());
        pe.setPrice(product.getPrice());
        pe.setQuantity(product.getQuantity());
        return pe;
    }

    private static Product toProduct(ProductEntity pe) {
        return new Product(
                pe.getId(),
                pe.getName(),
                pe.getPrice(),
                pe.getQuantity()
        );
    }

    public static Order toDomain(OrderRequest request) {
        List<Product> products = request.products().stream()
                .map(pr -> new Product(
                        pr.id(),      // id do produto vindo da requisição
                        pr.name(),
                        pr.price(),
                        pr.quantity()
                ))
                .collect(Collectors.toList());

        return new Order(request.externalId(), products);
    }
}
