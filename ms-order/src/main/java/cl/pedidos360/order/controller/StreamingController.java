package cl.pedidos360.order.controller;

import cl.pedidos360.order.messaging.kafka.OrderAnalyticsConsumer;
import cl.pedidos360.order.model.OrderAudit;
import cl.pedidos360.order.repository.OrderAuditRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/stream")
public class StreamingController {

    private final OrderAnalyticsConsumer analytics;
    private final OrderAuditRepository auditRepository;

    public StreamingController(OrderAnalyticsConsumer analytics,
                               OrderAuditRepository auditRepository) {
        this.analytics = analytics;
        this.auditRepository = auditRepository;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
                "totalOrders", analytics.getTotalOrders(),
                "totalSales", analytics.getTotalSales());
    }

    @GetMapping("/audit")
    public List<OrderAudit> audit() {
        return auditRepository.findAll();
    }
}
