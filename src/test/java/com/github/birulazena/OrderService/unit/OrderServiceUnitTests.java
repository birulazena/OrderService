package com.github.birulazena.OrderService.unit;

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
import com.github.birulazena.OrderService.util.DataTestFactory;
import com.github.birulazena.OrderService.filter.OrderFilter;
import com.github.birulazena.OrderService.mapper.OrderMapper;
import com.github.birulazena.OrderService.repository.ItemRepository;
import com.github.birulazena.OrderService.repository.OrderRepository;
import com.github.birulazena.OrderService.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setupSecurity() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                null,
                null,
                List.of()
        );
        auth.setDetails(Map.of("userId", 1L));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createOrderUserExceptionTest() {
        CreateOrderRequestDto createOrderRequestDto = DataTestFactory.createOrderRequestDto();
        UserDto userDto = DataTestFactory.invalidUserDto();

        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        assertThrows(UserNotFoundException.class,
                () -> orderService.createOrder(createOrderRequestDto));
    }

    @Test
    void createOrderItemExceptionTest() {
        CreateOrderRequestDto createOrderRequestDto = DataTestFactory.createOrderRequestDto();
        UserDto userDto = DataTestFactory.validuserDto();
        List<Item> itemRepositoryResult = new ArrayList<>(List.of(new Item(
                1L,
                "Book",
                BigDecimal.valueOf(10),
                null
        )));

        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        Mockito.when(itemRepository.findAllById(List.of(1L, 2L))).thenReturn(itemRepositoryResult);

        assertThrows(ItemNotFoundException.class,
                () -> orderService.createOrder(createOrderRequestDto));
    }

    @Test
    void createOrderTest() {
        CreateOrderRequestDto createOrderRequestDto = DataTestFactory.createOrderRequestDto();
        UserDto userDto = DataTestFactory.validuserDto();
        List<Item> items = new ArrayList<>(List.of(
                new Item(1L,
                        "Book",
                        BigDecimal.valueOf(20),
                        null),
                new Item(2L,
                        "Laptop",
                        BigDecimal.valueOf(40),
                        null)
        ));

        Order order = DataTestFactory.newOrder();
        Order saveOrder = DataTestFactory.newOrder();
        saveOrder.setId(1L);
        CreateOrderResponseDto createOrderResponseDto = DataTestFactory.createOrderResponseDto();
        createOrderResponseDto = createOrderResponseDto.addUserDto(userDto);

        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        Mockito.when(itemRepository.findAllById(List.of(1L, 2L))).thenReturn(items);
        Mockito.when(orderMapper.toEntity(createOrderRequestDto)).thenReturn(order);
        Mockito.when(orderRepository.save(order)).thenReturn(saveOrder);
        Mockito.when(orderMapper.toCreateOrderResponseDto(saveOrder)).thenReturn(createOrderResponseDto);

        CreateOrderResponseDto result = orderService.createOrder(createOrderRequestDto);

        assertEquals(result, createOrderResponseDto);
    }

    @Test
    void getOrderByIdOrderExceptionTest() {
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(1L));
    }

    @Test
    void getOrderByIdUserExceptionTest() {
        Order order = DataTestFactory.newOrder();
        order.setId(1L);

        UserDto userDto = DataTestFactory.invalidUserDto();

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        assertThrows(UserNotFoundException.class,
                () -> orderService.getOrderById(1L));
    }

    @Test
    void getOrderByIdTest() {
        Order order = DataTestFactory.newOrder();
        order.setId(1L);

        UserDto userDto = DataTestFactory.validuserDto();

        OrderResponseDto orderResponseDto = DataTestFactory.orderResponseDto();
        orderResponseDto = orderResponseDto.addUserDto(userDto);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        Mockito.when(orderMapper.toOrderResponseDto(order)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.getOrderById(1L);

        assertEquals(result, orderResponseDto);
    }

    @Test
    void getAllOrdersTest() {
        OrderFilter orderFilter = new OrderFilter(Status.PAID, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Order order1 = DataTestFactory.newOrder();
        order1.setId(1L);
        order1.setStatus(Status.PAID);
        Order order2 = DataTestFactory.newOrder();
        List<Order> orderList = new ArrayList<>(List.of(
                order1,
                order2
        ));
        Page<Order> orders = new PageImpl<>(
                orderList,
                PageRequest.of(0, 10),
                orderList.size()
        );

        UserDto userDto = DataTestFactory.validuserDto();

        OrderInfoResponseDto orderInfoResponseDto = DataTestFactory.orderInfoResponseDto();
        orderInfoResponseDto = orderInfoResponseDto.addUserDto(userDto);

        Mockito.when(orderRepository.findAll(Mockito.<Specification<Order>>any(), Mockito.eq(pageable)))
                .thenReturn(orders);
        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        Mockito.when(orderMapper.toOrderInfoResponseDto(Mockito.any())).thenReturn(orderInfoResponseDto);

        Page<OrderInfoResponseDto> result = orderService.getAllOrders(orderFilter, pageable);

        assertEquals(result.getContent().get(0), orderInfoResponseDto);
    }

    @Test
    void getOrdersByUserIdUserException() {
        UserDto userDto = DataTestFactory.invalidUserDto();
        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        assertThrows(UserNotFoundException.class,
                () -> orderService.getOrdersByUserId(1L));
    }

    @Test
    void getOrderByUserIdTest() {
        UserDto userDto = DataTestFactory.validuserDto();
        Order order1 = DataTestFactory.newOrder();
        order1.setId(1L);
        Order order2 = DataTestFactory.newOrder();
        order2.setId(2L);
        List<Order> orders = List.of(order1, order2);

        OnlyOrderInfoResponseDto dto1 = DataTestFactory.onlyOrderInfoResponseDto();
        OnlyOrderInfoResponseDto dto2 = DataTestFactory.onlyOrderInfoResponseDto();

        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        Mockito.when(orderRepository.findByUserId(1L)).thenReturn(orders);
        Mockito.when(orderMapper.toOnlyOrderInfoResponseDto(order1)).thenReturn(dto1);
        Mockito.when(orderMapper.toOnlyOrderInfoResponseDto(order2)).thenReturn(dto2);

        UserOrdersResponseDto result = orderService.getOrdersByUserId(1L);

        assertEquals(2, result.orders().size());
        assertEquals(dto1, result.orders().get(0));
        assertEquals(dto2, result.orders().get(1));
        assertEquals(userDto, result.userDto());
    }

    @Test
    void updateOrderByIdOrderExceptionTest() {
        UpdateOrderInfoRequestDto updateOrderInfoRequestDto = DataTestFactory.updateOrderInfoRequestDto();
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrderById(1L, updateOrderInfoRequestDto));
    }

    @Test
    void updateOrderByIdUserExceptionTest() {
        UpdateOrderInfoRequestDto updateOrderInfoRequestDto = DataTestFactory.updateOrderInfoRequestDto();
        UserDto userDto = DataTestFactory.invalidUserDto();
        Order order = DataTestFactory.newOrder();
        order.setId(1L);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);

        assertThrows(UserNotFoundException.class,
                () -> orderService.updateOrderById(1L, updateOrderInfoRequestDto));
    }

    @Test
    void updateOrderByIdTest() {
        UpdateOrderInfoRequestDto updateOrderInfoRequestDto = DataTestFactory.updateOrderInfoRequestDto();
        UserDto userDto = DataTestFactory.validuserDto();
        Order order = DataTestFactory.newOrder();
        order.setId(1L);
        OrderInfoResponseDto orderInfoResponseDto = DataTestFactory.orderInfoResponseDto();
        orderInfoResponseDto = orderInfoResponseDto.addUserDto(userDto);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Mockito.when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        Mockito.when(orderMapper.toOrderInfoResponseDto(order)).thenReturn(orderInfoResponseDto);

        OrderInfoResponseDto result = orderService.updateOrderById(1L, updateOrderInfoRequestDto);

        assertEquals(result, orderInfoResponseDto);
    }

    @Test
    void deleteOrderByIdTest() {
        Order order = DataTestFactory.newOrder();
        order.setId(1L);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        Mockito.verify(orderRepository).softDeleteById(1L);
    }

    @Test
    void deleteOrderByIdOrderExceptionTest() {
        Order order = DataTestFactory.newOrder();
        order.setId(1L);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.deleteOrderById(1L));
    }

    @Test
    void deleteOrderByIdStatusExceptionTest() {
        Order order = DataTestFactory.newOrder();
        order.setId(1L);
        order.setStatus(Status.PAID);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(WrongStatusException.class,
                () -> orderService.deleteOrderById(1L));
    }


}
