package cl.pedidos360.order.messaging.service;

import cl.pedidos360.order.dto.event.KitchenTicketMessage;
import cl.pedidos360.order.dto.event.OrderCreatedEvent;
import cl.pedidos360.order.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/** Traduce la entidad Order a los mensajes de RabbitMQ (el negocio no conoce los DTO de mensajería). */
@Component
public class OrderEventMapper {

    public OrderCreatedEvent toOrderCreated(Order order) {
        List<OrderCreatedEvent.Item> items = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(i -> new OrderCreatedEvent.Item(
                                i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity()))
                        .toList();

        return new OrderCreatedEvent(order.getId(), order.getUserEmail(), order.getTotalAmount(),
                order.getDispatchMethod(), order.getCreatedAt(), items);
    }

    public KitchenTicketMessage toKitchenTicket(Order order) {
        List<KitchenTicketMessage.Line> lines = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(i -> new KitchenTicketMessage.Line(i.getProductName(), i.getQuantity()))
                        .toList();

        return new KitchenTicketMessage(order.getId(), order.getDispatchMethod(), order.getCreatedAt(), lines);
    }
}
