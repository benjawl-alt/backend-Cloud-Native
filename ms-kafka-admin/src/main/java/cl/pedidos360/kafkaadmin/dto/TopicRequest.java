package cl.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record TopicRequest(

        @NotBlank(message = "El nombre del tópico es obligatorio")
        @Pattern(
                regexp = "^(?!__)(?!\\.{1,2}$)[A-Za-z0-9._-]{1,249}$",
                message = "Nombre inválido: utiliza letras, números, puntos, guiones o guiones bajos"
        )
        String name,

        @Min(value = 1, message = "partitions debe ser >= 1")
        Integer partitions,

        @Min(value = 1, message = "replicationFactor debe ser >= 1")
        Integer replicationFactor,

        Map<String, String> configs
) {

    public TopicRequest {
        if (partitions == null) {
            partitions = 3;
        }

        if (replicationFactor == null) {
            replicationFactor = 1;
        }
    }
}
