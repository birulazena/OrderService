package com.github.birulazena.OrderService.dto.request.order;

import com.github.birulazena.OrderService.dto.request.order_item.OrderItemRequestDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequestDto(@NotNull(message = "items must not be null")
                                    @NotEmpty(message = "order must contain at leat one item")
                                    List<OrderItemRequestDto> items) {
}
