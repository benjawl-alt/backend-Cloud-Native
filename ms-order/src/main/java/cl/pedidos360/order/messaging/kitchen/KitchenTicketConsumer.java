package cl.pedidos360.order.messaging.kitchen;

import cl.pedidos360.order.dto.event.KitchenTicketMessage;
import cl.pedidos360.order.messaging.common.AckHandler;
import cl.pedidos360.order.messaging.common.EventMessageParser;
import cl.pedidos360.order.messaging.common.InvalidEventException;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor de comandas de cocina. */
@Component
public class KitchenTicketConsumer {

    private final EventMessageParser parser;
    private final KitchenTicketHandler handler;
    private final AckHandler ackHandler;

    public KitchenTicketConsumer(EventMessageParser parser, KitchenTicketHandler handler, AckHandler ackHandler) {
        this.parser = parser;
        this.handler = handler;
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${pedidos360.rabbit.queues.kitchen}", ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel) throws IOException {
        String queue = message.getMessageProperties().getConsumerQueue();
        try {
            KitchenTicketMessage ticket = parser.parse(message, KitchenTicketMessage.class);
            handler.handle(ticket);
            ackHandler.ack(channel, message);
        } catch (InvalidEventException ex) {
            ackHandler.rejectToDlq(channel, message, queue, ex.getMessage());
        } catch (Exception ex) {
            ackHandler.retryOnceOrDlq(channel, message, queue, ex);
        }
    }
}
