package cl.pedidos360.order.messaging.dlq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OPCIONAL (pedidos360.rabbit.dlq-logger-enabled=true): consume las DLQ y deja registro en log.
 * Mientras esté en false, los mensajes quedan guardados en la DLQ para inspeccionarlos en la consola web.
 */
@Component
@ConditionalOnProperty(name = "pedidos360.rabbit.dlq-logger-enabled", havingValue = "true")
public class DeadLetterLogger {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterLogger.class);

    @RabbitListener(queues = {
            "${pedidos360.rabbit.queues.notification-dlq}",
            "${pedidos360.rabbit.queues.kitchen-dlq}"}, ackMode = "MANUAL")
    public void onDeadLetter(Message message, Channel channel) throws IOException {
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        log.error("[DLQ-LOG] cola={} x-death={} cuerpo={}",
                message.getMessageProperties().getConsumerQueue(), xDeath,
                new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
