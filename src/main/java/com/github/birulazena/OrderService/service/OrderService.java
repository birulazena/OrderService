package com.github.birulazena.OrderService.service;

import com.github.birulazena.OrderService.client.UserServiceClient;
import com.github.birulazena.OrderService.dto.request.order.CreateOrderRequestDto;
import com.github.birulazena.OrderService.dto.request.order.UpdateOrderInfoRequestDto;
import com.github.birulazena.OrderService.dto.response.order.*;
import com.github.birulazena.OrderService.dto.user.UserDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.entity.Order;
import com.github.birulazena.OrderService.entity.Status;
import com.github.birulazena.OrderService.exception.ItemNotFoundException;
import com.github.birulazena.OrderService.exception.OrderNotFoundException;
import com.github.birulazena.OrderService.exception.UserNotFoundException;
import com.github.birulazena.OrderService.exception.WrongStatusException;
import com.github.birulazena.OrderService.filter.OrderFilter;
import com.github.birulazena.OrderService.mapper.OrderMapper;
import com.github.birulazena.OrderService.repository.ItemRepository;
import com.github.birulazena.OrderService.repository.OrderRepository;
import com.github.birulazena.OrderService.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ItemRepository itemRepository;

    private final OrderMapper orderMapper;

    private final UserServiceClient userServiceClient;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreateOrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        Long userId = ((Map<String, Long>)authentication.getDetails()).get("userId");

        UserDto userDto = userServiceClient.getUserById(userId);
            if(userDto.name() == null)
                throw new UserNotFoundException("User with id " + userId + " not found");

        List<Long> itemIds = createOrderRequestDto.items().stream()
                .map(i -> i.itemId())
                .toList();

        Map<Long, Item> items = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        if(items.size() != itemIds.size())
            throw new ItemNotFoundException("Some items not found");

        Order order = orderMapper.toEntity(createOrderRequestDto);
        BigDecimal totalPrice = BigDecimal.ZERO;
        for(var oi : order.getOrderItems()) {
            oi.setOrder(order);
            oi.setItem(items.get(oi.getItem().getId()));
        }
        order.setTotalPrice(order.calculateTotalPrice());
        order.setUserId(userId);
        Order saveOrder = orderRepository.save(order);

        CreateOrderResponseDto createOrderResponseDto = orderMapper
                .toCreateOrderResponseDto(saveOrder);
        return createOrderResponseDto.addUserDto(userDto);
    }

    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + id + " not found"));

        UserDto userDto = userServiceClient.getUserById(order.getUserId());
        if(userDto.name() == null)
            throw new UserNotFoundException("User with id " + userDto.id() + " not found");

        OrderResponseDto orderResponseDto = orderMapper.toOrderResponseDto(order);
        return orderResponseDto.addUserDto(userDto);
    }

    public Page<OrderInfoResponseDto> getAllOrders(OrderFilter orderFilter, Pageable pageable) {
        Specification<Order> specification = Specification
                .where(OrderSpecification.hasStatus(orderFilter.status()))
                .and(OrderSpecification.timeBetween(orderFilter.startTime(), orderFilter.endTime()));
        Page<Order> orders = orderRepository.findAll(specification, pageable);
        Page<OrderInfoResponseDto> results = orders.map(order -> {
           UserDto userDto = userServiceClient.getUserById(order.getUserId());
           if (userDto.name() == null)
               throw new UserNotFoundException("User with id " + userDto.id() + " not found");
           OrderInfoResponseDto orderInfoResponseDto = orderMapper.toOrderInfoResponseDto(order);
           return orderInfoResponseDto.addUserDto(userDto);
        });
        return results;
    }

    public UserOrdersResponseDto getOrdersByUserId(Long userId) {
        UserDto userDto = userServiceClient.getUserById(userId);
        if (userDto.name() == null)
            throw new UserNotFoundException("User with id " + userDto.id() + " not found");

        List<Order> orders = orderRepository.findByUserId(userId);
        List<OnlyOrderInfoResponseDto> ordersDto = orders.stream()
                .map(order -> orderMapper.toOnlyOrderInfoResponseDto(order))
                .toList();
        return new UserOrdersResponseDto(ordersDto, userDto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderInfoResponseDto updateOrderById(Long id, UpdateOrderInfoRequestDto updateOrderInfoRequestDto) {
        if(!Status.isValid(updateOrderInfoRequestDto.status().toString()))
            throw new WrongStatusException("Wrong status");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + id + " not found"));
        order.setStatus(updateOrderInfoRequestDto.status());

        UserDto userDto = userServiceClient.getUserById(order.getUserId());
        if(userDto.name() == null)
            throw new UserNotFoundException("User with id " + userDto.id() + " not found");

        OrderInfoResponseDto orderInfoResponseDto = orderMapper.toOrderInfoResponseDto(order);
        return orderInfoResponseDto.addUserDto(userDto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFoundException("Order with id " + id + " not found")
        );
        if(!order.getStatus().equals(Status.CREATED))
            throw new WrongStatusException("Order cannot be deleted because its status is " + order.getStatus());
        orderRepository.softDeleteById(id);
    }
}
