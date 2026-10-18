package cl.pedidos360.order.messaging.producer;

import cl.pedidos360.order.config.RabbitNamesProperties;
import cl.pedidos360.order.dto.event.KitchenTicketMessage;
import cl.pedidos360.order.messaging.common.JsonMessageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Caso de uso: enviar la comanda a la cocina por el exchange direct. */
@Component
public class KitchenTicketPublisher {

    private static final Logger log = LoggerFactory.getLogger(KitchenTicketPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitNamesProperties names;
    private final JsonMessageSupport json;

    public KitchenTicketPublisher(RabbitTemplate rabbitTemplate, RabbitNamesProperties names, JsonMessageSupport json) {
        this.rabbitTemplate = rabbitTemplate;
        this.names = names;
        this.json = json;
    }

    public void publish(KitchenTicketMessage message) {
        rabbitTemplate.send(
                names.exchanges().direct(),
                names.routingKeys().kitchenTicket(),
                json.toMessage(message, message.orderId()),
                new CorrelationData(message.orderId()));
        log.info("[PRODUCER] kitchen.ticket publicado. orderId={}", message.orderId());
    }
}
