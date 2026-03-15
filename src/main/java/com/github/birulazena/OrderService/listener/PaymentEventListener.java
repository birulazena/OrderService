package com.github.birulazena.OrderService.listener;

import com.github.birulazena.OrderService.dto.request.event.PaymentEventDto;
import com.github.birulazena.OrderService.dto.request.order.UpdateOrderInfoRequestDto;
import com.github.birulazena.OrderService.entity.Order;
import com.github.birulazena.OrderService.entity.Status;
import com.github.birulazena.OrderService.exception.OrderNotFoundException;
import com.github.birulazena.OrderService.repository.OrderRepository;
import com.github.birulazena.OrderService.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final ObjectMapper objectMapper;

    private final OrderService orderService;

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "${KAFKA_TOPIC_PAYMENT_EVENTS_NAME}", groupId = "${KAFKA_ORDER_SERVICE_GROUP}")
    public void listenPaymentEvents(@Payload @Valid PaymentEventDto paymentEventDto) {
        Order order = orderRepository.findById(paymentEventDto.orderId()).orElseThrow(() ->
                new OrderNotFoundException("Order with id " + paymentEventDto.orderId() + " not found"));

        if(paymentEventDto.status().equals("SUCCESS")
                && paymentEventDto.userId().equals(order.getUserId())
                && paymentEventDto.paymentAmount().compareTo(order.getTotalPrice()) == 0
                && !order.getStatus().equals(Status.PAID)) {
            orderService.updateOrderById(paymentEventDto.orderId(), new UpdateOrderInfoRequestDto(Status.PAID));
        }
    }

}
