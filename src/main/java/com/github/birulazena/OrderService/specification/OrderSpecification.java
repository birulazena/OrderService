package com.github.birulazena.OrderService.specification;

import com.github.birulazena.OrderService.entity.Order;
import com.github.birulazena.OrderService.entity.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if(status == null)
                return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> timeBetween(LocalDateTime startTime,
                                                   LocalDateTime endTime) {
        return (root, query, criteriaBuilder) -> {
            if(startTime == null && endTime == null)
                return criteriaBuilder.conjunction();
            if(startTime != null && endTime == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime);
            if(startTime == null && endTime != null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime);
            return criteriaBuilder.between(root.get("createdAt"), startTime, endTime);
        };
    }
}
