package com.github.birulazena.OrderService.dto.response.order;

import com.github.birulazena.OrderService.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OnlyOrderInfoResponseDto(Long id,
                                       Status status,
                                       BigDecimal totalPrice,
                                       Boolean deleted,
                                       LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
}
