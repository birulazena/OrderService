package com.github.birulazena.OrderService.repository;

import com.github.birulazena.OrderService.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    Optional<Order> findById(Long id);

    Page<Order> findAll(Specification specification, Pageable pageable);

    List<Order> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Order o SET o.deleted = true WHERE o.id = :id")
    void softDeleteById(Long id);
}
