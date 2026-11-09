package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.LowStockEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LowStockAlertConsumer {

    private final KafkaEventParser parser;
    private final StockAlertService alertService;

    public LowStockAlertConsumer(KafkaEventParser parser, StockAlertService alertService) {
        this.parser = parser;
        this.alertService = alertService;
    }

    @KafkaListener(topics = "${app.kafka.topics.low-stock}", groupId = "pedidos360-order-lowstock-alerts")
    public void consume(String payload) {
        alertService.register(parser.parse(payload, LowStockEvent.class));
    }
}