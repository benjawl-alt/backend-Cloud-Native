package cl.pedidos360.order.messaging.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/** Convierte el cuerpo JSON a DTO y lo valida (Bean Validation). Lanza InvalidEventException si no sirve. */
@Component
public class EventMessageParser {

    private final JsonMessageSupport json;
    private final Validator validator;

    public EventMessageParser(JsonMessageSupport json, Validator validator) {
        this.json = json;
        this.validator = validator;
    }

    public <T> T parse(Message message, Class<T> type) {
        T payload;
        try {
            payload = json.fromMessage(message, type);
        } catch (RuntimeException e) {
            throw new InvalidEventException("JSON inválido para " + type.getSimpleName() + ": " + e.getMessage());
        }
        if (payload == null) {
            throw new InvalidEventException("Cuerpo vacío para " + type.getSimpleName());
        }
        Set<ConstraintViolation<T>> violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            String detail = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new InvalidEventException("DTO inválido (" + type.getSimpleName() + "): " + detail);
        }
        return payload;
    }
}
