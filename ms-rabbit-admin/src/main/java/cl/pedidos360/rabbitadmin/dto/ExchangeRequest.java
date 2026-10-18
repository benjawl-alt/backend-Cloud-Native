package cl.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = NamePattern.NAME, message = "Nombre inválido: solo letras, números, '.', '_' o '-' (máx. 100) y no puede empezar con 'amq.'")
        String name,

        @NotNull(message = "El tipo es obligatorio (DIRECT, TOPIC, FANOUT o HEADERS)")
        ExchangeType type,

        Boolean durable) {

    public ExchangeRequest {
        if (durable == null) durable = true;
    }
}
