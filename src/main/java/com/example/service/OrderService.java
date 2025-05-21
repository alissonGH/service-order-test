package com.example.service;

import com.example.domain.Order;
import com.example.domain.OrderStatus;
import com.example.exception.InternalException;
import com.example.kafka.producer.OrderProducer;
import com.example.mapper.OrderMapper;
import com.example.model.OrderEntity;
import com.example.model.ProductEntity;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderRedisService redisService;
    @Autowired
    private OrderProducer orderProducer;
    @Autowired
    private ProductRepository productRepository;

    public void processOrder(Order order) {
        String externalId = order.getExternalId();
        if (redisService.findByExternalId(externalId).isPresent()) {
            log.info("Order already exists: {}", externalId);
            return;
        }
        redisService.save(order);
        orderProducer.send(order);
    }

    @Transactional
    public void processOrderAsync(Order order) {
        String externalId = order.getExternalId();
        if (orderRepository.findByExternalId(externalId).isPresent()) {
            log.info("Order already exists: {}", externalId);
            return;
        }
        try {
            order.calculateTotal();
            order.updateStatus(OrderStatus.PROCESSED.name());
            orderRepository.save(toEntity(order));
            redisService.save(order);
            log.info("Order processed: {}", externalId);
        } catch (Exception e) {
            throw new InternalException("Order process fail.", e);
        }
    }

    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setExternalId(order.getExternalId());
        entity.setTotalValue(order.getTotalValue());
        entity.setStatus(Enum.valueOf(OrderStatus.class, order.getStatus()));

        Set<ProductEntity> productsFromDb = order.getProducts().stream()
                .map(p -> productRepository.findById(p.getId())
                        .orElseThrow(() -> new InternalException("Product not found with id: " + p.getId())))
                .collect(Collectors.toSet());

        entity.setProducts(productsFromDb);

        return entity;
    }

    public Order getOrderByExternalId(String externalId) {
        return redisService.findByExternalId(externalId)
                .or(() -> {
                    OrderEntity entity = orderRepository.findByExternalId(externalId)
                            .orElseThrow(() -> new InternalException(String.format("Order not found: %s", externalId)));
                    Order order = OrderMapper.toDomain(entity);
                    redisService.save(order);
                    return Optional.of(order);
                })
                .orElseThrow(() -> new InternalException(String.format("Order not found in cache and DB: %s", externalId)));
    }

}
