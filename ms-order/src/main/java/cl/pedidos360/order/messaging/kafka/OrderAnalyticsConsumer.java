package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Component
public class OrderAnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderAnalyticsConsumer.class);

    private final ObjectMapper objectMapper;
    private final AtomicLong totalOrders = new AtomicLong();
    private final DoubleAdder totalSales = new DoubleAdder();

    public OrderAnalyticsConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.orders-created}",
            groupId = "pedidos360-analytics")
    public void consume(String payload) throws Exception {
        OrderStreamEvent event = objectMapper.readValue(payload, OrderStreamEvent.class);
        totalOrders.incrementAndGet();
        totalSales.add(event.totalAmount() == null ? 0.0 : event.totalAmount());
        log.info("[ANALYTICS] Pedido procesado: {}", event.orderId());
    }

    // Estos contadores viven en memoria y se reinician al reiniciar ms-order.
    public long getTotalOrders() { return totalOrders.get(); }
    public double getTotalSales() { return totalSales.sum(); }
}
