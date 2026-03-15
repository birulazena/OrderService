package com.github.birulazena.OrderService.dto.request.event;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEventDto(@NotNull(message = "OrderId cannot be null")
                              Long orderId,
                              @NotNull(message = "UserId cannot be null")
                              Long userId,
                              @NotBlank(message = "Status cannot be empty")
                              @NotNull(message = "Status cannot be null")
                              String status,
                              @DecimalMin(value = "0.01", message = "Payment amount must be greater that zero")
                              BigDecimal paymentAmount,
                              @NotNull(message = "Timestamp cannot be null")
                              Instant timestamp) {
}
