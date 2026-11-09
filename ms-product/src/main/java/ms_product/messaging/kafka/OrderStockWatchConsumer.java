package ms_product.messaging.kafka;

import ms_product.dto.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStockWatchConsumer {

    private final KafkaEventParser parser;
    private final StockWatchService stockWatchService;

    public OrderStockWatchConsumer(KafkaEventParser parser, StockWatchService stockWatchService) {
        this.parser = parser;
        this.stockWatchService = stockWatchService;
    }

    @KafkaListener(topics = "${app.kafka.topics.orders-created}", groupId = "pedidos360-product-stock-watch")
    public void consume(String payload) {
        stockWatchService.check(parser.parse(payload, OrderCreatedEvent.class));
    }
}