package com.github.birulazena.OrderService.dto.response.order;

import com.github.birulazena.OrderService.dto.user.UserDto;

import java.util.List;

public record UserOrdersResponseDto(List<OnlyOrderInfoResponseDto> orders,
                                    UserDto userDto) {
}
