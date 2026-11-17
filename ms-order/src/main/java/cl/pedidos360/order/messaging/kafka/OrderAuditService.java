package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import cl.pedidos360.order.model.OrderAudit;
import cl.pedidos360.order.repository.OrderAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderAuditService {

    private static final Logger log = LoggerFactory.getLogger(OrderAuditService.class);

    private final OrderAuditRepository auditRepository;

    public OrderAuditService(OrderAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void register(OrderStreamEvent event, String rawPayload) {
        auditRepository.save(new OrderAudit(
                event.orderId(), event.userEmail(), event.totalAmount(),
                event.eventType(), rawPayload));
        log.info("[AUDIT] Pedido {} registrado desde Kafka", event.orderId());
    }
}