package ms_product.messaging.common;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** ACK explícito: OK -> ack; irrecuperable -> DLQ; recuperable -> 1 reintento y luego DLQ. Siempre con log. */
@Component
public class AckHandler {

    private static final Logger log = LoggerFactory.getLogger(AckHandler.class);

    public void ack(Channel channel, Message message) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    public void rejectToDlq(Channel channel, Message message, String queue, String reason) throws IOException {
        log.error("[DLQ] Mensaje no entregable. cola={} motivo={} cuerpo={}", queue, reason, body(message));
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }

    public void retryOnceOrDlq(Channel channel, Message message, String queue, Exception ex) throws IOException {
        if (message.getMessageProperties().isRedelivered()) {
            rejectToDlq(channel, message, queue, "reintento agotado: " + ex.getMessage());
        } else {
            log.warn("[RETRY] Falla recuperable en cola={} ({}). Se reintenta una vez.", queue, ex.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    private String body(Message message) {
        String s = new String(message.getBody(), StandardCharsets.UTF_8);
        return s.length() > 500 ? s.substring(0, 500) + "…" : s;
    }
}
