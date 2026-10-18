package cl.pedidos360.order.messaging.notification;

import cl.pedidos360.order.dto.event.OrderCreatedEvent;
import cl.pedidos360.order.messaging.common.AckHandler;
import cl.pedidos360.order.messaging.common.EventMessageParser;
import cl.pedidos360.order.messaging.common.InvalidEventException;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor de notificaciones. Solo orquesta: parsear/validar -> handler -> ACK/NACK. */
@Component
public class OrderNotificationConsumer {

    private final EventMessageParser parser;
    private final OrderNotificationHandler handler;
    private final AckHandler ackHandler;

    public OrderNotificationConsumer(EventMessageParser parser, OrderNotificationHandler handler, AckHandler ackHandler) {
        this.parser = parser;
        this.handler = handler;
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${pedidos360.rabbit.queues.notification}", ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel) throws IOException {
        String queue = message.getMessageProperties().getConsumerQueue();
        try {
            OrderCreatedEvent event = parser.parse(message, OrderCreatedEvent.class);
            handler.handle(event);
            ackHandler.ack(channel, message);
        } catch (InvalidEventException ex) {
            ackHandler.rejectToDlq(channel, message, queue, ex.getMessage());
        } catch (Exception ex) {
            ackHandler.retryOnceOrDlq(channel, message, queue, ex);
        }
    }
}
