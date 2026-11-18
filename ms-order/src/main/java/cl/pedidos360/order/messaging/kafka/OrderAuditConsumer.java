package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditConsumer {

    private final KafkaEventParser parser;
    private final OrderAuditService auditService;

    public OrderAuditConsumer(KafkaEventParser parser, OrderAuditService auditService) {
        this.parser = parser;
        this.auditService = auditService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.orders-created}",
            groupId = "pedidos360-audit")
    public void consume(String payload) {
        OrderStreamEvent event = parser.parse(payload, OrderStreamEvent.class);
        auditService.register(event, payload);
    }
}