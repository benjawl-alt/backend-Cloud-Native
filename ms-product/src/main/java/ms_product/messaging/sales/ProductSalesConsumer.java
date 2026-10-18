package ms_product.messaging.sales;

import com.rabbitmq.client.Channel;
import ms_product.dto.event.OrderCreatedEvent;
import ms_product.messaging.common.AckHandler;
import ms_product.messaging.common.EventMessageParser;
import ms_product.messaging.common.InvalidEventException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor de ventas: parsear/validar -> servicio de negocio -> ACK/NACK explícito. */
@Component
public class ProductSalesConsumer {

    private final EventMessageParser parser;
    private final ProductSalesService salesService;
    private final AckHandler ackHandler;

    public ProductSalesConsumer(EventMessageParser parser, ProductSalesService salesService, AckHandler ackHandler) {
        this.parser = parser;
        this.salesService = salesService;
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${pedidos360.rabbit.queues.sales}", ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel) throws IOException {
        String queue = message.getMessageProperties().getConsumerQueue();
        try {
            OrderCreatedEvent event = parser.parse(message, OrderCreatedEvent.class);
            salesService.register(event);
            ackHandler.ack(channel, message);
        } catch (InvalidEventException ex) {
            ackHandler.rejectToDlq(channel, message, queue, ex.getMessage());
        } catch (Exception ex) {
            ackHandler.retryOnceOrDlq(channel, message, queue, ex);
        }
    }
}
