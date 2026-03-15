package com.github.birulazena.OrderService.dto.response.order;

import com.github.birulazena.OrderService.dto.response.order_item.OrderItemResponseDto;
import com.github.birulazena.OrderService.dto.user.UserDto;
import com.github.birulazena.OrderService.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(Long id,
                               Status status,
                               BigDecimal totalPrice,
                               Boolean deleted,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt,
                               List<OrderItemResponseDto> orderItems,
                               UserDto userDto) {

    public OrderResponseDto addUserDto(UserDto userDto) {
        return new OrderResponseDto(
                id,
                status,
                totalPrice,
                deleted,
                createdAt,
                updatedAt,
                orderItems,
                userDto
        );
    }
}
