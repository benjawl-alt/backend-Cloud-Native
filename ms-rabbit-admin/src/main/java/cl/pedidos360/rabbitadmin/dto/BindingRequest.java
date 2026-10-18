package cl.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BindingRequest(
        @NotBlank(message = "El exchange es obligatorio")
        @Pattern(regexp = NamePattern.NAME, message = "Nombre de exchange inválido")
        String exchange,

        @NotBlank(message = "La cola es obligatoria")
        @Pattern(regexp = NamePattern.NAME, message = "Nombre de cola inválido")
        String queue,

        @Pattern(regexp = NamePattern.ROUTING_KEY, message = "Routing key inválida")
        String routingKey) {

    public BindingRequest {
        if (routingKey == null) routingKey = "";
    }
}
