package com.github.birulazena.OrderService.integration;

import com.github.birulazena.OrderService.dto.request.order.CreateOrderRequestDto;
import com.github.birulazena.OrderService.dto.request.order.UpdateOrderInfoRequestDto;
import com.github.birulazena.OrderService.dto.request.order_item.OrderItemRequestDto;
import com.github.birulazena.OrderService.dto.response.order.CreateOrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderInfoResponseDto;
import com.github.birulazena.OrderService.dto.response.order.OrderResponseDto;
import com.github.birulazena.OrderService.dto.response.order.UserOrdersResponseDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.entity.Order;
import com.github.birulazena.OrderService.entity.Status;
import com.github.birulazena.OrderService.repository.ItemRepository;
import com.github.birulazena.OrderService.repository.OrderRepository;
import com.github.birulazena.OrderService.util.DataTestFactory;
import com.github.birulazena.OrderService.util.JwtServiceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @Autowired
    JwtServiceTest jwtServiceTest;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    static Long BOOK_ID;
    static Long LAPTOP_ID;
    static Long ORDER1_ID;
    static Long ORDER2_ID;
    static Long ORDER3_ID_WITHOUT_USER;
    static Long ORDER4_ID_PAID_STATUS;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("users")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static WireMockContainer wireMockContainer = new WireMockContainer("wiremock/wiremock:3.13.1")
            .withMapping("user1", """
                    {
                    "request": {
                    "method": "GET", 
                    "url": "/api/v1/users/1"
                    },
                    "response": {
                    "status": 200, 
                    "body": "{\\"id\\":1,\\"name\\":\\"Zenya\\",\\"surname\\":\\"Birulya\\",\\"birthDate\\":\\"2006-06-28\\",\\"email\\":\\"birulazena@gmail.com\\",\\"active\\":true,\\"createdAt\\":\\"2026-01-01T10:00:00\\",\\"updatedAt\\":\\"2026-01-01T10:00:00\\"}",
                    "headers": {
                    "Content-Type": "application/json"
                    }
                    }
                    }
                    """)
            .withMapping("user2", """
                    {
                    "request": {
                    "method": "GET", 
                    "url": "/api/v1/users/2"
                    },
                    "response": {
                    "status": 404, 
                    "body": "{\\"message\\":\\"User with id 2 not found\\"}",
                    "headers": { "Content-Type": "application/json" }
                    }
                    }
                    """);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("user-service.url", () -> wireMockContainer.getBaseUrl() + "/api/v1/users");
    }

    @BeforeAll
    static void createItems(@Autowired ItemRepository itemRepository,
                            @Autowired OrderRepository orderRepository) {
        BOOK_ID = itemRepository.save(new Item(null, "Book", BigDecimal.valueOf(20), null)).getId();
        LAPTOP_ID = itemRepository.save(new Item(null, "Laptop", BigDecimal.valueOf(40), null)).getId();

        Order order = DataTestFactory.newOrder();
        ORDER1_ID = orderRepository.save(order).getId();
        Order order1 = DataTestFactory.newOrder();
        ORDER2_ID = orderRepository.save(order1).getId();
        Order order2 = DataTestFactory.newOrder();
        order2.setUserId(2L);
        ORDER3_ID_WITHOUT_USER = orderRepository.save(order2).getId();
        Order order3 = DataTestFactory.newOrder();
        order3.setStatus(Status.PAID);
        ORDER4_ID_PAID_STATUS = orderRepository.save(order3).getId();
    }

    @Test
    @Transactional
    void createOrderSuccess() throws Exception{
        CreateOrderRequestDto createOrderRequestDto = DataTestFactory.createOrderRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequestDto))
        )
                .andExpect(status().isCreated())
                .andReturn();

        CreateOrderResponseDto createOrderResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CreateOrderResponseDto.class
        );

        assertEquals(1L, createOrderResponseDto.userDto().id());
        assertEquals(Status.CREATED, createOrderResponseDto.status());
        assertEquals(0, createOrderResponseDto.totalPrice().compareTo(BigDecimal.valueOf(100)));
        assertTrue(createOrderResponseDto.orderItems().stream()
                .anyMatch(oi -> oi.itemId().equals(BOOK_ID)));
        assertTrue(createOrderResponseDto.orderItems().stream()
                .anyMatch(oi -> oi.itemId().equals(LAPTOP_ID)));
        assertNotNull(createOrderResponseDto.id());
    }

    @Test
    void createOrderItemError() throws Exception{
        CreateOrderRequestDto createOrderRequestDto = new CreateOrderRequestDto(List.of(
                new OrderItemRequestDto(1L, 3),
                new OrderItemRequestDto(3L, 1)
        ));
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createOrderRequestDto))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Some items not found"))
                .andReturn();
    }

    @Test
    void createOrderUserError() throws Exception {
        CreateOrderRequestDto createOrderRequestDto = DataTestFactory.createOrderRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 2L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/orders")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createOrderRequestDto))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 2 not found"))
                .andReturn();
    }

    @Test
    void getOrderByIdSuccessful() throws Exception {
        Long id = ORDER1_ID;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto orderResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                OrderResponseDto.class
        );

        assertEquals(ORDER1_ID, orderResponseDto.id());
        assertTrue(orderResponseDto.orderItems().stream()
                .anyMatch(oi -> oi.itemId().equals(BOOK_ID)));
        assertTrue(orderResponseDto.orderItems().stream()
                .anyMatch(oi -> oi.itemId().equals(LAPTOP_ID)));
    }

    @Test
    void getOrderByIdNotEnoughRights() throws Exception {
        Long id = ORDER1_ID;
        String token = jwtServiceTest.generateAccessToken("Zenya", 2L, "USER");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    void getOrderByIdOrderError() throws Exception{
        Long id = 5L;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id " + id + " not found"))
                .andReturn();
    }

    @Test
    void getOrderByIdUserError() throws Exception{
        Long id = ORDER3_ID_WITHOUT_USER;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id 2 not found"))
                .andReturn();
    }

    @Test
    void getAllOrdersSuccessful() throws Exception {
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders")
                                .param("page", "0")
                                .param("size", "10")
                                .param("status", "PAID")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("content");

        Set<Long> ids = new HashSet<>();
        content.forEach(node -> ids.add(node.get("id").asLong()));
        assertEquals(1, ids.size());
    }

    @Test
    void getAllOrdersSuccessfulTwo() throws Exception {
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders")
                                .param("page", "0")
                                .param("size", "2")
                                .param("status", "CREATED")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("content");

        Set<Long> ids = new HashSet<>();
        content.forEach(node -> ids.add(node.get("id").asLong()));
        assertEquals(2, ids.size());
    }

    @Test
    void getAllOrdersNotEnoughRights() throws Exception{
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders")
                                .param("page", "0")
                                .param("size", "2")
                                .param("status", "CREATED")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    void getOrdersByUserIdSuccessful() throws Exception{
        Long userId = 1L;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders/user/" + userId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        UserOrdersResponseDto userOrdersResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserOrdersResponseDto.class
        );

        assertEquals(1L, userOrdersResponseDto.userDto().id());
        assertEquals(3L, userOrdersResponseDto.orders().size());
        assertTrue(userOrdersResponseDto.orders().stream()
                .anyMatch(o -> o.id().equals(BOOK_ID)));
        assertTrue(userOrdersResponseDto.orders().stream()
                .anyMatch(o -> o.id().equals(LAPTOP_ID)));

    }

    @Test
    void getOrdersByUserIdNotEnoughRights() throws Exception{
        Long userId = 1L;
        String token = jwtServiceTest.generateAccessToken("Zenya", 2L, "USER");
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/orders/user/" + userId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    @Transactional
    void updateOrderByIdSuccessful() throws Exception {
        UpdateOrderInfoRequestDto updateOrderInfoRequestDto = DataTestFactory.updateOrderInfoRequestDto();
        Long id = ORDER1_ID;
        String token = jwtServiceTest.generateAccessToken("Zenya", 2L, "ADMIN");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateOrderInfoRequestDto))
                )
                .andExpect(status().isOk())
                .andReturn();

        OrderInfoResponseDto orderInfoResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                OrderInfoResponseDto.class
        );

        assertEquals(id, orderInfoResponseDto.id());
        assertEquals(updateOrderInfoRequestDto.status(), orderInfoResponseDto.status());
        assertNotNull(orderInfoResponseDto.userDto());
    }

    @Test
    void updateOrderByIdNotEnoughRights() throws Exception {
        UpdateOrderInfoRequestDto updateOrderInfoRequestDto = DataTestFactory.updateOrderInfoRequestDto();
        Long id = ORDER1_ID;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateOrderInfoRequestDto))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    void updateOrderByIdOrderError() throws Exception{
        UpdateOrderInfoRequestDto updateOrderInfoRequestDto = DataTestFactory.updateOrderInfoRequestDto();
        Long id = 5L;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateOrderInfoRequestDto))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id " + id + " not found"))
                .andReturn();
    }

    @Test
    @Transactional
    void deleteOrderByIdSuccessful() throws Exception{
        Long id = ORDER1_ID;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    void deleteOrderByIdNotEnoughRights() throws Exception {
        Long id = ORDER1_ID;
        String token = jwtServiceTest.generateAccessToken("Zenya", 2L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    @Transactional
    void deleteOrderByIdWrongStatusError() throws Exception{
        Long id = ORDER4_ID_PAID_STATUS;
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/orders/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order cannot be deleted because its status is PAID"))
                .andReturn();
    }
}
