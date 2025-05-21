package com.example.kafka.producer;

import com.example.domain.Order;
import com.example.service.MapperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private MapperService objectMapper;
    @Value("${topic.order}")
    private String topicName;

    public void send(Order order) {
        String message = objectMapper.serialize(order);
        log.info("Order sent: {}", order.getExternalId());
        kafkaTemplate.send(topicName, order.getExternalId(), message);
    }


}
