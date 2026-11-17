package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderAnalyticsConsumer {

    private final KafkaEventParser parser;
    private final OrderAnalyticsService analyticsService;

    public OrderAnalyticsConsumer(KafkaEventParser parser, OrderAnalyticsService analyticsService) {
        this.parser = parser;
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.orders-created}",
            groupId = "pedidos360-analytics")
    public void consume(String payload) {
        analyticsService.register(parser.parse(payload, OrderStreamEvent.class));
    }
}