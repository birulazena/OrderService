package com.github.birulazena.OrderService.dto.response.item;

import java.math.BigDecimal;

public record ItemResponseDto(Long id,
                              String name,
                              BigDecimal price) {
}
