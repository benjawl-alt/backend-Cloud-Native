package cl.pedidos360.order.controller;

import cl.pedidos360.order.dto.event.LowStockEvent;
import cl.pedidos360.order.messaging.kafka.OrderAnalyticsService;
import cl.pedidos360.order.model.OrderAudit;
import cl.pedidos360.order.repository.OrderAuditRepository;
import cl.pedidos360.order.streams.InventoryQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/stream")
public class StreamingController {

    private final OrderAnalyticsService analytics;
    private final OrderAuditRepository auditRepository;
    private final InventoryQueryService inventory;

    public StreamingController(OrderAnalyticsService analytics,
                               OrderAuditRepository auditRepository,
                               InventoryQueryService inventory) {
        this.analytics = analytics;
        this.auditRepository = auditRepository;
        this.inventory = inventory;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics(@AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(Map.of(
                "totalOrders", analytics.getTotalOrders(),
                "totalSales", analytics.getTotalSales()));
    }

    @GetMapping("/audit")
    public ResponseEntity<List<OrderAudit>> audit(@AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(auditRepository.findAll());
    }

    /** Alertas de stock bajo (state store de Kafka Streams). */
    @GetMapping("/alerts")
    public ResponseEntity<List<LowStockEvent>> alerts(@AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(inventory.lowStockAlerts());
    }

    /** Productos más demandados (state store de Kafka Streams). */
    @GetMapping("/top-products")
    public ResponseEntity<List<Map<String, Object>>> topProducts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "5") int limit) {
        if (!isAdmin(jwt)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(inventory.topProducts(Math.max(1, Math.min(limit, 20))));
    }

    private boolean isAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && (roles.contains("Admin")
                || roles.contains("Administrator")
                || roles.contains("Administrador"));
    }
}