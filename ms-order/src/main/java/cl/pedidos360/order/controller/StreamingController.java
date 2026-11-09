package cl.pedidos360.order.controller;

import cl.pedidos360.order.dto.event.LowStockEvent;
import cl.pedidos360.order.messaging.kafka.OrderAnalyticsConsumer;
import cl.pedidos360.order.model.OrderAudit;
import cl.pedidos360.order.repository.OrderAuditRepository;
import cl.pedidos360.order.messaging.kafka.StockAlertService;

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
    private final StockAlertService stockAlertService;

    public StreamingController(
            OrderAnalyticsConsumer analytics,
            OrderAuditRepository auditRepository,
            StockAlertService stockAlertService) {

        this.analytics = analytics;
        this.auditRepository = auditRepository;
        this.stockAlertService = stockAlertService;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
                "totalOrders", analytics.getTotalOrders(),
                "totalSales", analytics.getTotalSales());
    }

    @GetMapping("/alerts")
    public List<LowStockEvent> alerts() {
        return stockAlertService.recent();
    }

    @GetMapping("/audit")
    public List<OrderAudit> audit() {
        return auditRepository.findAll();
    }
}
