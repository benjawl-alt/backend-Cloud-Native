package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import cl.pedidos360.order.model.OrderAudit;
import cl.pedidos360.order.repository.OrderAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderAuditConsumer.class);

    private final OrderAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public OrderAuditConsumer(OrderAuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.orders-created}",
            groupId = "pedidos360-audit")
    public void consume(String payload) throws Exception {
        OrderStreamEvent event = objectMapper.readValue(payload, OrderStreamEvent.class);
        auditRepository.save(new OrderAudit(
                event.orderId(),
                event.userEmail(),
                event.totalAmount(),
                event.eventType(),
                payload));
        log.info("[AUDIT] Pedido {} registrado desde Kafka", event.orderId());
    }
}
