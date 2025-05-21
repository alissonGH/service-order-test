package com.example.kafka.consumer;

import com.example.service.MapperService;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private MapperService objectMapper;

    @KafkaListener(topics = "order-topic", groupId = "order-group", containerFactory = "orderListenerContainerFactory")
    public void receiveOrder(String payload, Acknowledgment acknowledgment) {
        var order = objectMapper.deserialize(payload);
        log.info("Order received: {}", order.getExternalId());
        orderService.processOrderAsync(order);
        acknowledgment.acknowledge();
    }

}