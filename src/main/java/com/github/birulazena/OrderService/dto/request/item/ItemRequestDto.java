package com.github.birulazena.OrderService.dto.request.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemRequestDto(@NotBlank(message = "name must not be blank")
                             @Size(max = 255, message = "name must be no more 255 characters")
                             String name,
                             @NotNull(message = "Price must not be null")
                             @DecimalMin(value = "0.01", message = "price must be at least 0.01")
                             BigDecimal price) {
}
