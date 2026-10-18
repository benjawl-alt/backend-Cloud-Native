package cl.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = NamePattern.NAME, message = "Nombre inválido: solo letras, números, '.', '_' o '-' (máx. 100) y no puede empezar con 'amq.'")
        String name,

        Boolean durable,

        /** true = crea también la DLQ "<name>.dlq" y la asocia al DLX */
        Boolean withDlq,

        @Min(value = 1, message = "messageTtlMs debe ser >= 1")
        Integer messageTtlMs,

        @Min(value = 1, message = "maxLength debe ser >= 1")
        Long maxLength) {

    public QueueRequest {
        if (durable == null) durable = true;
        if (withDlq == null) withDlq = true;
    }
}
