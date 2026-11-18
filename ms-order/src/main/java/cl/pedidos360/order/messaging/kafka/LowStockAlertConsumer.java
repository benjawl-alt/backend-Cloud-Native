package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.LowStockEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Payload;

@Component
public class LowStockAlertConsumer {

    private final KafkaEventParser parser;
    private final StockAlertService alertService;

    public LowStockAlertConsumer(KafkaEventParser parser, StockAlertService alertService) {
        this.parser = parser;
        this.alertService = alertService;
    }

    @KafkaListener(topics = "${app.kafka.topics.low-stock}", groupId = "pedidos360-order-lowstock-alerts")
    public void consume(@Payload(required = false) String payload) {
        if (payload == null) return; // tombstone: alerta resuelta, nada que registrar
        alertService.register(parser.parse(payload, LowStockEvent.class));
    }
}