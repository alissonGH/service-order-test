package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderRequest(
        @NotBlank String externalId,
        @NotEmpty List<ProductRequest> products
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductRequest(
            @NotNull Long id,
            @NotBlank String name,
            @NotNull BigDecimal price,
            @NotNull int quantity
    ) {}
}
