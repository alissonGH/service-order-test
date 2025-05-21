package com.example.service;

import com.example.domain.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
public class OrderRedisService {

    private static final String PREFIX = "order::";
    @Autowired
    private MapperService objectMapper;
    @Autowired
    private RedisTemplate<String, String> orderRedisTemplate;

    public void save(Order order) {
        String key = buildKey(order.getExternalId());
        String message = objectMapper.serialize(order);
        orderRedisTemplate.opsForValue().set(key, message);
        log.info("Order saved: {}", key);
    }

    public Optional<Order> findByExternalId(String externalId) {
        String key = buildKey(externalId);
        var orderFound = orderRedisTemplate.opsForValue().get(key);
        if (!ObjectUtils.isEmpty(orderFound)) {
            Order order = objectMapper.deserialize(orderFound);
            return Optional.ofNullable(order);
        }
        return Optional.empty();
    }

    public void delete(String externalId) {
        String key = buildKey(externalId);
        orderRedisTemplate.delete(key);
    }

    private String buildKey(String externalId) {
        return PREFIX + externalId;
    }
}
