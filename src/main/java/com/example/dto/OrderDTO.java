package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDTO(
        String status,
        String externalId,
        List<ProductDTO> products
) {}
