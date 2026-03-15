package com.github.birulazena.OrderService.repository;

import com.github.birulazena.OrderService.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
