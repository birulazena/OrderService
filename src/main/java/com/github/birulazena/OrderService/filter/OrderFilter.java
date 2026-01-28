package com.github.birulazena.OrderService.filter;

import com.github.birulazena.OrderService.entity.Status;

import java.time.LocalDateTime;

public record OrderFilter(Status status,
                          LocalDateTime startTime,
                          LocalDateTime endTime) {
}
