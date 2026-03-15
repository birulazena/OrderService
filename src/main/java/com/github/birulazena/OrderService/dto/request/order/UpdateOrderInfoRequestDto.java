package com.github.birulazena.OrderService.dto.request.order;

import com.github.birulazena.OrderService.entity.Status;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateOrderInfoRequestDto(@NotNull(message = "status mast not be null")
                                        Status status) {
}

