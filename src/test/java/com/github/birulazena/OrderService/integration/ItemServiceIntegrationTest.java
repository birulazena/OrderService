package com.github.birulazena.OrderService.integration;

import com.github.birulazena.OrderService.dto.request.item.ItemRequestDto;
import com.github.birulazena.OrderService.dto.response.item.ItemResponseDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.repository.ItemRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemServiceIntegrationTest {

    @Autowired
    JwtServiceTest jwtServiceTest;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    static Long BOOK_ID;
    static Long LAPTOP_ID;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("users")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void createItems(@Autowired ItemRepository itemRepository) {
        BOOK_ID = itemRepository.save(new Item(null, "Book", BigDecimal.valueOf(20), null)).getId();
        LAPTOP_ID = itemRepository.save(new Item(null, "Laptop", BigDecimal.valueOf(40), null)).getId();
    }

    @Test
    @Transactional
    void createItemSuccess() throws Exception{
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn();

        ItemResponseDto itemResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ItemResponseDto.class
        );

        assertNotNull(itemResponseDto.id());
        assertEquals(itemRequestDto.name(), itemResponseDto.name());
        assertEquals(itemRequestDto.price(), itemResponseDto.price());
    }

    @Test
    void saveItemNotEnoughRights() throws Exception{
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    @Transactional
    void updateItemSuccessful() throws Exception{
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");
        Long id = LAPTOP_ID;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                )
                .andExpect(status().isOk())
                .andReturn();

        ItemResponseDto itemResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ItemResponseDto.class
        );

        assertEquals(id, itemResponseDto.id());
        assertEquals(itemRequestDto.name(), itemResponseDto.name());
        assertEquals(itemRequestDto.price(), itemResponseDto.price());
    }

    @Test
    void updateItemItemError() throws Exception{
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");
        Long id = 4L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item with id " + id + " not found"))
                .andReturn();
    }

    @Test
    void updateItemNotEnoughRights() throws Exception{
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        Long id = BOOK_ID;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    @Transactional
    void deleteByIdSuccessful() throws Exception{
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "ADMIN");
        Long id = BOOK_ID;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    void deleteNotEnoughRight() throws Exception{
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        Long id = BOOK_ID;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have sufficient rights to perform this"))
                .andReturn();
    }

    @Test
    void getItemByIdSuccessful() throws Exception{
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        Long id = BOOK_ID;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        ItemResponseDto itemResponseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ItemResponseDto.class
        );

        assertEquals(id, itemResponseDto.id());
        assertEquals("Book", itemResponseDto.name());
        assertEquals(0, itemResponseDto.price().compareTo(BigDecimal.valueOf(20)));
    }

    @Test
    void getItemByIdItemException() throws Exception{
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");
        Long id = 4L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/items/" + id)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item with id " + id + " not found"))
                .andReturn();
    }

    @Test
    void getAllItemsSuccessful() throws Exception {
        String token = jwtServiceTest.generateAccessToken("Zenya", 1L, "USER");

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/items")
                                .header("Authorization", "Bearer " + token)
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("content");

        Set<Long> ids = new HashSet<>();
        content.forEach(node -> ids.add(node.get("id").asLong()));
        assertEquals(2, ids.size());
    }
}
