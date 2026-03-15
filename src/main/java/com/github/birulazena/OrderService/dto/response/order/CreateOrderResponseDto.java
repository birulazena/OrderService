package com.github.birulazena.OrderService.dto.response.order;

import com.github.birulazena.OrderService.dto.response.order_item.OrderItemResponseDto;
import com.github.birulazena.OrderService.dto.user.UserDto;
import com.github.birulazena.OrderService.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderResponseDto(Long id,
                                     Status status,
                                     BigDecimal totalPrice,
                                     LocalDateTime createdAt,
                                     List<OrderItemResponseDto> orderItems,
                                     UserDto userDto) {

    public CreateOrderResponseDto addUserDto(UserDto userDto) {
        return new CreateOrderResponseDto(
                id,
                status,
                totalPrice,
                createdAt,
                orderItems,
                userDto
        );
    }
}
