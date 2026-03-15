package com.github.birulazena.OrderService.mapper;

import com.github.birulazena.OrderService.dto.request.order.CreateOrderRequestDto;
import com.github.birulazena.OrderService.dto.response.order.CreateOrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OnlyOrderInfoResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderInfoResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderResponseDto;
import com.github.birulazena.OrderService.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(target = "orderItems", source = "items")
    Order toEntity(CreateOrderRequestDto createOrderRequestDto);

    CreateOrderResponseDto toCreateOrderResponseDto(Order order);

    OrderResponseDto toOrderResponseDto(Order order);

    OrderInfoResponseDto toOrderInfoResponseDto(Order order);

    OnlyOrderInfoResponseDto toOnlyOrderInfoResponseDto(Order order);

}
