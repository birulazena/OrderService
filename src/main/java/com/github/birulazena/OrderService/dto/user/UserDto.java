package com.github.birulazena.OrderService.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDto(@NotNull(message = "id must not be null") Long id,
                      @NotBlank(message = "name must not be empty") String name,
                      @NotBlank(message = "surname must not be empty") String surname,
                      @NotNull(message = "birthDate must not be null")
                      @Past(message = "birthDate must be in the past")
                      LocalDate birthDate,
                      @NotBlank(message = "email must not be empty") String email,
                      @NotNull(message = "active must not be null") Boolean active,
                      @NotNull(message = "createdAt must not be null")
                      @Past(message = "createdAt must be in the past")
                      LocalDateTime createdAt,
                      @NotNull(message = "updatedAt must not be null")
                      @Past(message = "updatedAt must be in the past")
                      LocalDateTime updatedAt) {
}