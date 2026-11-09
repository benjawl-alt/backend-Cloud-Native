package ms_product.dto.event;

import java.time.Instant;

public record LowStockEvent(String productId, String productName,
                            Integer currentStock, Integer threshold, Instant detectedAt) {}