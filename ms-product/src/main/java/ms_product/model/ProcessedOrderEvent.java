package ms_product.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Marca de idempotencia: un mismo pedido no suma dos veces aunque RabbitMQ lo reentregue. */
@Document(collection = "processed_order_events")
public class ProcessedOrderEvent {

    @Id
    private String orderId;
    private Instant processedAt;

    public ProcessedOrderEvent() {}

    public ProcessedOrderEvent(String orderId, Instant processedAt) {
        this.orderId = orderId;
        this.processedAt = processedAt;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
