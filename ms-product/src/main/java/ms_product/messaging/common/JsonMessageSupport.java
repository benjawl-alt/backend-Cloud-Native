package ms_product.messaging.common;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Lee el JSON de los mensajes (Jackson 3, el de Spring Boot 4). */
@Component
public class JsonMessageSupport {

    private final JsonMapper mapper = JsonMapper.builder().build();

    public <T> T fromMessage(Message message, Class<T> type) {
        return mapper.readValue(message.getBody(), type);
    }
}
