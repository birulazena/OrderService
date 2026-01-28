package com.github.birulazena.OrderService.dto.request.order_item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDto(@NotNull(message = "itemId mast not be null")
                                  Long itemId,
                                  @NotNull(message = "quantity mast not be null")
                                  @Min(value = 1, message = "quantity must be at least 1")
                                  Integer quantity) {
}
