package cl.pedidos360.order.messaging.common;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Convierte DTO <-> Message JSON (Jackson 3, el de Spring Boot 4). Un solo lugar para toda la serialización. */
@Component
public class JsonMessageSupport {

    private final JsonMapper mapper = JsonMapper.builder().build();

    public Message toMessage(Object payload, String messageId) {
        byte[] body = mapper.writeValueAsBytes(payload);
        return MessageBuilder.withBody(body)
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(messageId)
                .build();
    }

    public <T> T fromMessage(Message message, Class<T> type) {
        return mapper.readValue(message.getBody(), type);
    }
}
