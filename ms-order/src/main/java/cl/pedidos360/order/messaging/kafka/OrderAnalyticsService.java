package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Service
public class OrderAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(OrderAnalyticsService.class);

    private final AtomicLong totalOrders = new AtomicLong();
    private final DoubleAdder totalSales = new DoubleAdder();

    public void register(OrderStreamEvent event) {
        totalOrders.incrementAndGet();
        totalSales.add(event.totalAmount() == null ? 0.0 : event.totalAmount());
        log.info("[ANALYTICS] Pedido procesado: {}", event.orderId());
    }

    // Contadores en memoria: se reinician al reiniciar ms-order.
    public long getTotalOrders() { return totalOrders.get(); }
    public double getTotalSales() { return totalSales.sum(); }
}