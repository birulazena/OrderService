package com.github.birulazena.OrderService.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.birulazena.OrderService.exception.OrderNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);

        handler.addNotRetryableExceptions(
                JsonProcessingException.class,
                MethodArgumentNotValidException.class,
                OrderNotFoundException.class
        );

        return handler;
    }

    @Bean
    public RecordMessageConverter converter() {
        return new StringJacksonJsonMessageConverter();
    }
}
