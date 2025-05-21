package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDTO(
        String name,
        BigDecimal price,
        Integer quantity
) {}
