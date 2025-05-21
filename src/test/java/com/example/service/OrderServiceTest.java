package com.example.service;

import com.example.domain.Order;
import com.example.domain.OrderStatus;
import com.example.domain.Product;
import com.example.exception.InternalException;
import com.example.kafka.producer.OrderProducer;
import com.example.mapper.OrderMapper;
import com.example.model.OrderEntity;
import com.example.model.ProductEntity;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderRedisService redisService;

    @Mock
    private OrderProducer orderProducer;

    @Mock
    private ProductRepository productRepository;

    private Order order;

    @BeforeEach
    void setup() {
        order = new Order();
        order.setExternalId("ext-123");
        order.setStatus(OrderStatus.PENDING.name());
        order.setTotalValue(BigDecimal.valueOf(100.00));
        order.setProducts(List.of(
                new Product(1L, "Product 1", BigDecimal.valueOf(50), 1),
                new Product(2L, "Product 2", BigDecimal.valueOf(25), 2)
        ));
    }

    @Test
    void processOrder_whenOrderExistsInRedis_shouldLogAndReturn() {
        when(redisService.findByExternalId("ext-123")).thenReturn(Optional.of(order));

        orderService.processOrder(order);

        verify(redisService, never()).save(any());
        verify(orderProducer, never()).send(any());
    }

    @Test
    void processOrder_whenOrderNotInRedis_shouldSaveAndSend() {
        when(redisService.findByExternalId("ext-123")).thenReturn(Optional.empty());

        orderService.processOrder(order);

        verify(redisService).save(order);
        verify(orderProducer).send(order);
    }

    @Test
    void processOrderAsync_whenOrderExistsInDb_shouldLogAndReturn() {
        when(orderRepository.findByExternalId("ext-123")).thenReturn(Optional.of(new OrderEntity()));

        orderService.processOrderAsync(order);

        verify(orderRepository, never()).save(any());
        verify(redisService, never()).save(any());
    }

    @Test
    void processOrderAsync_whenOrderNotInDb_shouldProcessAndSave() {
        when(orderRepository.findByExternalId("ext-123")).thenReturn(Optional.empty());

        when(productRepository.findById(1L)).thenReturn(Optional.of(new ProductEntity()));
        when(productRepository.findById(2L)).thenReturn(Optional.of(new ProductEntity()));

        Order spyOrder = spy(order);
        doNothing().when(spyOrder).calculateTotal();
        doNothing().when(spyOrder).updateStatus(OrderStatus.PROCESSED.name());

        orderService.processOrderAsync(spyOrder);

        verify(orderRepository).save(any(OrderEntity.class));
        verify(redisService).save(spyOrder);
    }

    @Test
    void toEntity_shouldMapOrderToOrderEntity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(new ProductEntity()));
        when(productRepository.findById(2L)).thenReturn(Optional.of(new ProductEntity()));

        OrderEntity entity = orderService.toEntity(order);

        assertEquals(order.getExternalId(), entity.getExternalId());
        assertEquals(order.getTotalValue(), entity.getTotalValue());
        assertEquals(OrderStatus.valueOf(order.getStatus()), entity.getStatus());
        assertEquals(2, entity.getProducts().size());
    }

    @Test
    void getOrderByExternalId_whenInRedis_shouldReturnOrder() {
        when(redisService.findByExternalId("ext-123")).thenReturn(Optional.of(order));

        Order result = orderService.getOrderByExternalId("ext-123");

        assertEquals(order, result);
        verify(orderRepository, never()).findByExternalId(anyString());
    }

    @Test
    void getOrderByExternalId_whenNotInRedisButInDb_shouldReturnOrderAndCache() {
        when(redisService.findByExternalId("ext-123")).thenReturn(Optional.empty());

        ProductEntity product1 = new ProductEntity();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(50));

        ProductEntity product2 = new ProductEntity();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(25));

        OrderEntity entity = new OrderEntity();
        entity.setId(100L);
        entity.setExternalId("ext-123");
        entity.setStatus(OrderStatus.PENDING);
        entity.setTotalValue(BigDecimal.valueOf(100));
        entity.setProducts(Set.of(product1, product2));

        when(orderRepository.findByExternalId("ext-123")).thenReturn(Optional.of(entity));

        Order result = orderService.getOrderByExternalId("ext-123");

        assertNotNull(result);
        assertEquals("ext-123", result.getExternalId());
        assertFalse(result.getProducts().isEmpty());
        verify(redisService).save(any(Order.class));
    }

    @Test
    void getOrderByExternalId_whenNotFound_shouldThrowException() {
        when(redisService.findByExternalId("ext-123")).thenReturn(Optional.empty());
        when(orderRepository.findByExternalId("ext-123")).thenReturn(Optional.empty());

        InternalException ex = assertThrows(InternalException.class, () -> {
            orderService.getOrderByExternalId("ext-123");
        });

        assertTrue(ex.getMessage().contains("Order not found"));
    }
}
