package cl.pedidos360.order.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LowStockEvent(
        @NotBlank String productId,
        @NotBlank String productName,
        @NotNull @PositiveOrZero Integer currentStock,
        @NotNull Integer threshold,
        Instant detectedAt) {}