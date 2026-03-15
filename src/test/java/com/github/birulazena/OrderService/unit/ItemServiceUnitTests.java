package com.github.birulazena.OrderService.unit;

import com.github.birulazena.OrderService.dto.request.item.ItemRequestDto;
import com.github.birulazena.OrderService.dto.response.item.ItemResponseDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.exception.ItemNotFoundException;
import com.github.birulazena.OrderService.util.DataTestFactory;
import com.github.birulazena.OrderService.mapper.ItemMapper;
import com.github.birulazena.OrderService.repository.ItemRepository;
import com.github.birulazena.OrderService.service.ItemService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTests {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    @Test
    void saveItemTest() {
        Item item = DataTestFactory.newItem();
        Item saveItem = DataTestFactory.newItem();
        saveItem.setId(1L);
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        ItemResponseDto itemResponseDto = DataTestFactory.itemResponseDto();

        Mockito.when(itemMapper.toEntity(itemRequestDto)).thenReturn(item);
        Mockito.when(itemRepository.save(item)).thenReturn(saveItem);
        Mockito.when(itemMapper.toItemResponseDto(saveItem)).thenReturn(itemResponseDto);

        ItemResponseDto result = itemService.saveItem(itemRequestDto);

        assertEquals(result, itemResponseDto);
    }

    @Test
    void updateItem() {
        Item item = DataTestFactory.newItem();
        Item item1 = DataTestFactory.newItem();
        item1.setId(1L);
        Item updateItem = DataTestFactory.newItem();
        updateItem.setId(1L);
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();
        ItemResponseDto itemResponseDto = DataTestFactory.itemResponseDto();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemMapper.toEntity(itemRequestDto)).thenReturn(item);
        Mockito.when(itemRepository.save(item)).thenReturn(updateItem);
        Mockito.when(itemMapper.toItemResponseDto(updateItem)).thenReturn(itemResponseDto);

        ItemResponseDto result = itemService.updateItem(1L, itemRequestDto);

        assertEquals(result, itemResponseDto);
    }

    @Test
    void updateItemItemNotFoundException() {
        ItemRequestDto itemRequestDto = DataTestFactory.itemRequestDto();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(1L, itemRequestDto));
    }

    @Test
    void deleteByIdTest() {
        itemService.deleteById(1L);

        Mockito.verify(itemRepository).deleteById(1L);
    }

    @Test
    void getItemByIdItemExceptionTest() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> itemService.getItemById(1L));
    }

    @Test
    void getItemByIdTest() {
        Item item = DataTestFactory.newItem();
        item.setId(1L);
        ItemResponseDto itemResponseDto = DataTestFactory.itemResponseDto();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));;
        Mockito.when(itemMapper.toItemResponseDto(item)).thenReturn(itemResponseDto);

        ItemResponseDto result = itemService.getItemById(1L);

        assertEquals(result, itemResponseDto);
    }

    @Test
    void getAllItemsTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Item item1 = DataTestFactory.newItem();
        item1.setId(1L);
        Item item2 = DataTestFactory.newItem();
        item2.setId(2L);
        List<Item> itemList = List.of(item1, item2);
        Page<Item> items = new PageImpl<>(
                itemList,
                PageRequest.of(0, 10),
                itemList.size()
        );

        ItemResponseDto dto1 = DataTestFactory.itemResponseDto();
        ItemResponseDto dto2 = DataTestFactory.itemResponseDto();

        Mockito.when(itemRepository.findAll(pageable)).thenReturn(items);
        Mockito.when(itemMapper.toItemResponseDto(item1)).thenReturn(dto1);
        Mockito.when(itemMapper.toItemResponseDto(item2)).thenReturn(dto2);

        Page<ItemResponseDto> result = itemService.getAllItems(pageable);

        assertEquals(dto1, result.getContent().get(0));
        assertEquals(dto2, result.getContent().get(1));
    }
}
