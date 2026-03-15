package com.github.birulazena.OrderService.util;

import com.github.birulazena.OrderService.dto.request.item.ItemRequestDto;
import com.github.birulazena.OrderService.dto.request.order.CreateOrderRequestDto;
import com.github.birulazena.OrderService.dto.request.order.UpdateOrderInfoRequestDto;
import com.github.birulazena.OrderService.dto.request.order_item.OrderItemRequestDto;
import com.github.birulazena.OrderService.dto.response.item.ItemResponseDto;
import com.github.birulazena.OrderService.dto.response.order.CreateOrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OnlyOrderInfoResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderInfoResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order_item.OrderItemResponseDto;
import com.github.birulazena.OrderService.dto.user.UserDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.entity.Order;
import com.github.birulazena.OrderService.entity.OrderItem;
import com.github.birulazena.OrderService.entity.Status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataTestFactory {

    public static CreateOrderRequestDto createOrderRequestDto() {
        return new CreateOrderRequestDto(
                List.of(
                        new OrderItemRequestDto(1L, 3),
                        new OrderItemRequestDto(2L, 1)
                )
        );
    }

    public static CreateOrderResponseDto createOrderResponseDto() {

        UserDto userDto = new UserDto(
                1L,
                "Zenya",
                "Birulya",
                LocalDate.now(),
                UUID.randomUUID() + "@gmail.com",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<OrderItemResponseDto> items = new ArrayList<>(List.of(
                new OrderItemResponseDto(1L,
                        "Book",
                        BigDecimal.valueOf(20),
                        3),
                new OrderItemResponseDto(2L,
                        "Laptop" ,
                        BigDecimal.valueOf(40),
                        1)
        ));

        return new CreateOrderResponseDto(1L,
                Status.CREATED,
                BigDecimal.valueOf(100),
                LocalDateTime.now(),
                items,
                userDto);
    }

    public static UserDto validuserDto() {
        return new UserDto(
                1L,
                "Zenya",
                "Birulya",
                LocalDate.now(),
                UUID.randomUUID() + "@gmail.com",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static UserDto invalidUserDto() {
        return new UserDto(
                1L,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Order newOrder() {
        Order order = new Order();
        order.setUserId(1L);


        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(new Item(1L, "Book", BigDecimal.valueOf(20), null));
        orderItem.setQuantity(3);

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrder(order);
        orderItem1.setItem(new Item(2L, "Laptop", BigDecimal.valueOf(40), null));
        orderItem1.setQuantity(1);

        order.setOrderItems(List.of(orderItem, orderItem1));

        return order;
    }

    public static OrderResponseDto orderResponseDto() {
        List<OrderItemResponseDto> items = new ArrayList<>(List.of(
                new OrderItemResponseDto(1L,
                        "Book",
                        BigDecimal.valueOf(20),
                        3),
                new OrderItemResponseDto(2L,
                        "Laptop" ,
                        BigDecimal.valueOf(40),
                        1)
        ));

        return new OrderResponseDto(
                1L,
                Status.CREATED,
                BigDecimal.valueOf(100),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                items,
                null
        );
    }

    public static OrderInfoResponseDto orderInfoResponseDto() {
        return new OrderInfoResponseDto(
                1L,
                Status.PAID,
                BigDecimal.valueOf(100),
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    public static OnlyOrderInfoResponseDto onlyOrderInfoResponseDto() {
        return new OnlyOrderInfoResponseDto(
                1L,
                Status.CREATED,
                BigDecimal.valueOf(100),
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static UpdateOrderInfoRequestDto updateOrderInfoRequestDto() {
        return new UpdateOrderInfoRequestDto(
                Status.PAID
        );
    }

    public static ItemRequestDto itemRequestDto() {
        return new ItemRequestDto(
                "Book",
                BigDecimal.valueOf(20)
        );
    }

    public static ItemResponseDto itemResponseDto() {
        return new ItemResponseDto(
                1L,
                "Book",
                BigDecimal.valueOf(20)
        );
    }

    public static Item newItem() {
        return new Item(
                null,
                "Book",
                BigDecimal.valueOf(20),
                null
        );
    }

    public static void createUserJwtToken() {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        null, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        usernamePasswordAuthenticationToken.setDetails(
                Map.of("userId", 1L)
        );
        SecurityContextHolder.getContext()
                .setAuthentication(usernamePasswordAuthenticationToken);
    }
}
