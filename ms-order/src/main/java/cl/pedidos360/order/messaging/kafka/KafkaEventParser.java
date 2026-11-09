package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.messaging.common.InvalidEventException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KafkaEventParser {

    private final ObjectMapper mapper;
    private final Validator validator;

    public KafkaEventParser(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T parse(String payload, Class<T> type) {
        T event;
        try {
            event = mapper.readValue(payload, type);
        } catch (RuntimeException e) {
            throw new InvalidEventException("JSON inválido para " + type.getSimpleName() + ": " + e.getMessage());
        }
        if (event == null) {
            throw new InvalidEventException("Cuerpo vacío para " + type.getSimpleName());
        }
        Set<ConstraintViolation<T>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            String detail = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted().collect(Collectors.joining("; "));
            throw new InvalidEventException("DTO inválido (" + type.getSimpleName() + "): " + detail);
        }
        return event;
    }
}