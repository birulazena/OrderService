package com.github.birulazena.OrderService.controller;

import com.github.birulazena.OrderService.dto.request.item.ItemRequestDto;
import com.github.birulazena.OrderService.dto.response.item.ItemResponseDto;
import com.github.birulazena.OrderService.entity.Item;
import com.github.birulazena.OrderService.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody ItemRequestDto itemRequestDto) {
        ItemResponseDto itemResponseDto = itemService.saveItem(itemRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id,
                                                      @RequestBody ItemRequestDto itemRequestDto) {
        ItemResponseDto itemResponseDto = itemService.updateItem(id, itemRequestDto);
        return ResponseEntity.ok(itemResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemById(@PathVariable Long id) {
        itemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
        ItemResponseDto itemResponseDto = itemService.getItemById(id);
        return ResponseEntity.ok(itemResponseDto);
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponseDto>> getAllItems(Pageable pageable) {
        Page<ItemResponseDto> items = itemService.getAllItems(pageable);
        return ResponseEntity.ok(items);
    }

}
