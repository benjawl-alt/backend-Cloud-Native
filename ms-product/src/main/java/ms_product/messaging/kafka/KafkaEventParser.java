package ms_product.messaging.kafka;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ms_product.messaging.common.InvalidEventException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KafkaEventParser {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final Validator validator;

    public KafkaEventParser(Validator validator) {
        this.validator = validator;
    }

    public <T> T parse(String payload, Class<T> type) {
        T event;
        try {
            event = mapper.readValue(payload, type);
        } catch (RuntimeException e) {
            throw new InvalidEventException("JSON inválido para " + type.getSimpleName() + ": " + e.getMessage());
        }
        if (event == null) throw new InvalidEventException("Cuerpo vacío para " + type.getSimpleName());

        Set<ConstraintViolation<T>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new InvalidEventException("DTO inválido (" + type.getSimpleName() + "): "
                    + violations.stream().map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted().collect(Collectors.joining("; ")));
        }
        return event;
    }
}