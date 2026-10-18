package cl.pedidos360.order.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "order_audit")
public class OrderAudit {

    @Id
    private String id;
    private String orderId;
    private String userEmail;
    private Double totalAmount;
    private String eventType;
    private LocalDateTime receivedAt;
    private String rawPayload;

    public OrderAudit() {}

    public OrderAudit(String orderId, String userEmail, Double totalAmount,
                      String eventType, String rawPayload) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
        this.eventType = eventType;
        this.rawPayload = rawPayload;
        this.receivedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getUserEmail() { return userEmail; }
    public Double getTotalAmount() { return totalAmount; }
    public String getEventType() { return eventType; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public String getRawPayload() { return rawPayload; }
}
