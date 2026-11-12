package cl.pedidos360.order.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LowStockEvent(String productId, String productName,
                            Integer currentStock, Integer threshold, Instant detectedAt) {}