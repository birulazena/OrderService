package com.github.birulazena.OrderService.mapper;

import com.github.birulazena.OrderService.dto.request.order_item.OrderItemRequestDto;
import com.github.birulazena.OrderService.dto.response.order_item.OrderItemResponseDto;
import com.github.birulazena.OrderService.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "item.id", source = "itemId")
    OrderItem toEntity(OrderItemRequestDto orderItemRequestDto);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "price", source = "item.price")
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);
}
