package cl.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.Min;

import java.util.Map;

/** Modificación: solo se puede AUMENTAR el número de particiones. */
public record TopicUpdateRequest(
        @Min(value = 1, message = "partitions debe ser >= 1") Integer partitions,
        Map<String, String> configs) {}