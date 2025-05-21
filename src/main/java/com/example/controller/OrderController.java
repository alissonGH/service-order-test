package com.example.controller;

import com.example.domain.Order;
import com.example.dto.OrderRequest;
import com.example.mapper.OrderMapper;
import com.example.service.MapperService;
import com.example.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private MapperService mapperService;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderRequest orderRequest) {
        var order = OrderMapper.toDomain(orderRequest);
        orderService.processOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<Order> getOrderByExternalId(@PathVariable String externalId) {
        Order order = orderService.getOrderByExternalId(externalId);
        return ResponseEntity.ok(order);
    }
}
