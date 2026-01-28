package com.github.birulazena.OrderService.dto.response.order_item;

import java.math.BigDecimal;

public record OrderItemResponseDto(Long itemId,
                                   String name,
                                   BigDecimal price,
                                   Integer quantity) {
}
