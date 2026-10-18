package ms_product.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** Copia local (solo lo que ms-product necesita) del evento publicado por ms-order. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        @NotBlank String orderId,
        @NotEmpty List<@Valid Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String productId,
            @NotBlank String productName,
            @NotNull @Positive Integer quantity) {}
}
