package cl.pedidos360.order.dto.event;

import cl.pedidos360.order.model.Order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderStreamEvent(
        String eventType,

        @NotBlank
        String orderId,

        @NotBlank
        String userEmail,

        String status,

        @NotNull
        @PositiveOrZero
        Double totalAmount,

        String dispatchMethod,
        LocalDateTime createdAt,
        List<Item> items) {

    public record Item(
            String productId,
            String productName,
            Double unitPrice,
            Integer quantity) {}

    public static OrderStreamEvent from(String eventType, Order order) {

        List<Item> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                    .map(item -> new Item(
                            item.getProductId(),
                            item.getProductName(),
                            item.getUnitPrice(),
                            item.getQuantity()))
                    .toList();

        return new OrderStreamEvent(
                eventType,
                order.getId(),
                order.getUserEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDispatchMethod(),
                order.getCreatedAt(),
                items);
    }
}
