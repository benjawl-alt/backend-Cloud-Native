package ms_product.messaging.kafka;

import ms_product.dto.event.LowStockEvent;
import ms_product.dto.event.OrderCreatedEvent;
import ms_product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Revisa el stock de los productos de un pedido y emite alerta si quedó bajo el umbral. */
@Service
public class StockWatchService {

    private final ProductRepository productRepository;
    private final LowStockKafkaPublisher publisher;
    private final int threshold;

    public StockWatchService(ProductRepository productRepository, LowStockKafkaPublisher publisher,
                             @Value("${app.stock.low-threshold:10}") int threshold) {
        this.productRepository = productRepository;
        this.publisher = publisher;
        this.threshold = threshold;
    }

    public void check(OrderCreatedEvent event) {
        for (OrderCreatedEvent.Item item : event.items()) {
            if (item.productId() == null || item.productId().isBlank()) continue;
            productRepository.findById(item.productId()).ifPresent(p -> {
                if (p.getStock() != null && p.getStock() <= threshold) {
                    publisher.publish(new LowStockEvent(
                            p.getId(), p.getName(), p.getStock(), threshold, Instant.now()));
                }
            });
        }
    }
}