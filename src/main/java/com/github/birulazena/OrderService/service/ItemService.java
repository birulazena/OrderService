package com.github.birulazena.OrderService.service;

import com.github.birulazena.OrderService.dto.request.item.ItemRequestDto;
import com.github.birulazena.OrderService.dto.response.item.ItemResponseDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.exception.ItemNotFoundException;
import com.github.birulazena.OrderService.mapper.ItemMapper;
import com.github.birulazena.OrderService.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemResponseDto saveItem(ItemRequestDto itemRequestDto) {
        Item item = itemMapper.toEntity(itemRequestDto);
        Item saveItem = itemRepository.save(item);
        return itemMapper.toItemResponseDto(saveItem);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {
        itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Item with id " + id + " not found")
        );
        Item item = itemMapper.toEntity(itemRequestDto);
        item.setId(id);
        Item updatedItem = itemRepository.save(item);
        return itemMapper.toItemResponseDto(updatedItem);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    public ItemResponseDto getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() ->
                new ItemNotFoundException("Item with id " + id + " not found"));
        return itemMapper.toItemResponseDto(item);
    }

    public Page<ItemResponseDto> getAllItems(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(item -> itemMapper.toItemResponseDto(item));
    }

}
