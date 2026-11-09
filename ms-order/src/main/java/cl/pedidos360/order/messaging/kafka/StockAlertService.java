package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.LowStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class StockAlertService {

    private static final Logger log = LoggerFactory.getLogger(StockAlertService.class);
    private static final int MAX = 50;

    private final ConcurrentLinkedDeque<LowStockEvent> recent = new ConcurrentLinkedDeque<>();

    public void register(LowStockEvent event) {
        recent.addFirst(event);
        while (recent.size() > MAX) recent.pollLast();
        log.warn("[STOCK-ALERT] '{}' quedó con {} unidades (umbral {})",
                event.productName(), event.currentStock(), event.threshold());
    }

    public List<LowStockEvent> recent() {
        return new ArrayList<>(recent);
    }
}