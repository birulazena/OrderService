package com.github.birulazena.OrderService.client;

import com.github.birulazena.OrderService.dto.user.UserDto;
import com.github.birulazena.OrderService.security.service.JwtService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserServiceClient {

    private final WebClient webClient;

    private final JwtService jwtService;

    public UserServiceClient(WebClient.Builder builder,
                             @Value("${user-service.url}") String url,
                             JwtService jwtService) {
        this.webClient = builder.baseUrl(url).build();
        this.jwtService = jwtService;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserDto getUserById(Long id) {
        String token = jwtService.generateAdminAccessToken();

        return webClient.get()
                .uri("/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    public UserDto getUserByIdFallback(Long id, Throwable t) {
        return new UserDto(id, null, null, null,
                null, null, null, null);
    }

}
