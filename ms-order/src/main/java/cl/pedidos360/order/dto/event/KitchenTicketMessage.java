package cl.pedidos360.order.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

/** Comanda para la cocina (direct: kitchen.ticket). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KitchenTicketMessage(
        @NotBlank String orderId,
        String dispatchMethod,
        LocalDateTime createdAt,
        @NotEmpty List<@Valid Line> lines) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Line(
            @NotBlank String productName,
            @NotNull @Positive Integer quantity) {}
}
