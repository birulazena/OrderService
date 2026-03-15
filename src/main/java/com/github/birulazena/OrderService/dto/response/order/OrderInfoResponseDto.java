package com.github.birulazena.OrderService.dto.response.order;

import com.github.birulazena.OrderService.dto.response.order_item.OrderItemResponseDto;
import com.github.birulazena.OrderService.dto.user.UserDto;
import com.github.birulazena.OrderService.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderInfoResponseDto(Long id,
                                   Status status,
                                   BigDecimal totalPrice,
                                   Boolean deleted,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt,
                                   UserDto userDto) {

    public OrderInfoResponseDto addUserDto(UserDto userDto) {
        return new OrderInfoResponseDto(
                id,
                status,
                totalPrice,
                deleted,
                createdAt,
                updatedAt,
                userDto
        );
    }
}
