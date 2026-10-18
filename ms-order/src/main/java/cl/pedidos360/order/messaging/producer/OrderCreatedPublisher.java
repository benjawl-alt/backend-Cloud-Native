package cl.pedidos360.order.messaging.producer;

import cl.pedidos360.order.config.RabbitNamesProperties;
import cl.pedidos360.order.dto.event.OrderCreatedEvent;
import cl.pedidos360.order.messaging.common.JsonMessageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Caso de uso: publicar "pedido creado" en el exchange topic. */
@Component
public class OrderCreatedPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitNamesProperties names;
    private final JsonMessageSupport json;

    public OrderCreatedPublisher(RabbitTemplate rabbitTemplate, RabbitNamesProperties names, JsonMessageSupport json) {
        this.rabbitTemplate = rabbitTemplate;
        this.names = names;
        this.json = json;
    }

    public void publish(OrderCreatedEvent event) {
        rabbitTemplate.send(
                names.exchanges().topic(),
                names.routingKeys().orderCreated(),
                json.toMessage(event, event.orderId()),
                new CorrelationData(event.orderId()));
        log.info("[PRODUCER] order.created publicado. orderId={}", event.orderId());
    }
}
