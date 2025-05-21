package com.example.service;

import com.example.domain.Order;
import com.example.exception.InternalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MapperService {

    @Autowired
    private ObjectMapper objectMapper;

    public Order deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, Order.class);
        } catch (Exception ex) {
            throw new InternalException("Order serialization fail.");
        }
    }

    public String serialize(Order order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (Exception ex) {
            throw new InternalException("Order deserialization fail.");
        }
    }
}
