package cl.pedidos360.order.messaging.service;

import cl.pedidos360.order.messaging.producer.KitchenTicketPublisher;
import cl.pedidos360.order.messaging.producer.OrderCreatedPublisher;
import cl.pedidos360.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

/**
 * El broker se usa "como servicio": el negocio (OrderController) solo llama a este método.
 * Si RabbitMQ está caído el pedido NO se pierde ni falla: se registra el error en log.
 */
@Service
public class OrderMessagingService {

    private static final Logger log = LoggerFactory.getLogger(OrderMessagingService.class);

    private final OrderCreatedPublisher orderCreatedPublisher;
    private final KitchenTicketPublisher kitchenTicketPublisher;
    private final OrderEventMapper mapper;

    public OrderMessagingService(OrderCreatedPublisher orderCreatedPublisher,
                                 KitchenTicketPublisher kitchenTicketPublisher,
                                 OrderEventMapper mapper) {
        this.orderCreatedPublisher = orderCreatedPublisher;
        this.kitchenTicketPublisher = kitchenTicketPublisher;
        this.mapper = mapper;
    }

    public void publishOrderCreated(Order order) {
        try {
            orderCreatedPublisher.publish(mapper.toOrderCreated(order));
            kitchenTicketPublisher.publish(mapper.toKitchenTicket(order));
        } catch (AmqpException ex) {
            log.error("[PRODUCER] No se pudo publicar en RabbitMQ el pedido {}: {}", order.getId(), ex.getMessage());
        }
    }
}
