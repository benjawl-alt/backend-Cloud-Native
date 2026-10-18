package cl.pedidos360.order.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;

/** Evento "pedido creado" (topic: order.created). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        @NotBlank String orderId,
        @NotBlank String userEmail,
        @NotNull @PositiveOrZero Double totalAmount,
        String dispatchMethod,
        LocalDateTime createdAt,
        @NotEmpty List<@Valid Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String productId,
            @NotBlank String productName,
            @NotNull @Positive Double unitPrice,
            @NotNull @Positive Integer quantity) {}
}
