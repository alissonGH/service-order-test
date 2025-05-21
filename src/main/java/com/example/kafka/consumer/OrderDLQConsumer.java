package com.example.kafka.consumer;

import com.example.service.MapperService;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderDLQConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private MapperService objectMapper;

    @KafkaListener(topics = "order-dlq", groupId = "order-group")
    public void receiveOrder(String payload) {
        log.info("DLQ Order received.");
    }
}
