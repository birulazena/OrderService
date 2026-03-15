package com.github.birulazena.OrderService.controller;

import com.github.birulazena.OrderService.dto.request.order.CreateOrderRequestDto;
import com.github.birulazena.OrderService.dto.request.order.UpdateOrderInfoRequestDto;
import com.github.birulazena.OrderService.dto.response.order.CreateOrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderInfoResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order.UserOrdersResponseDto;
import com.github.birulazena.OrderService.filter.OrderFilter;
import com.github.birulazena.OrderService.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponseDto> createOrder(@Valid @RequestBody
                                                                  CreateOrderRequestDto createOrderRequestDto) {
        CreateOrderResponseDto createOrderResponseDto = orderService.createOrder(createOrderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderResponseDto);
    }

    @PostAuthorize("returnObject.body.userDto.id == authentication.details['userId'] or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto orderResponseDto = orderService.getOrderById(id);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OrderInfoResponseDto>> getAllOrders(OrderFilter orderFilter, Pageable pageable) {
        Page<OrderInfoResponseDto> page = orderService.getAllOrders(orderFilter, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("#userId == authentication.details['userId'] or hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserOrdersResponseDto> getOrdersByUserId(@PathVariable Long userId) {
        UserOrdersResponseDto userOrdersResponseDto = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(userOrdersResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<OrderInfoResponseDto> updateOrderById(@PathVariable Long id,
                                                                @RequestBody
                                                                UpdateOrderInfoRequestDto updateOrderInfoRequestDto) {
        OrderInfoResponseDto orderInfoResponseDto = orderService.updateOrderById(id, updateOrderInfoRequestDto);
        return ResponseEntity.ok(orderInfoResponseDto);
    }

    @PreAuthorize("@orderSecurity.belongsToUser(#id, authentication.details['userId']) or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

}
