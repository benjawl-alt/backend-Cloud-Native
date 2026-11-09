package ms_product.messaging.kafka;

import ms_product.dto.event.LowStockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class LowStockKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(LowStockKafkaPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final String topic;

    public LowStockKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                  @Value("${app.kafka.topics.low-stock}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(LowStockEvent event) {
        try {
            // clave = productId -> mismo producto siempre a la misma partición (y compactación)
            kafkaTemplate.send(topic, event.productId(), mapper.writeValueAsString(event))
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("[KAFKA] No se pudo publicar low-stock de {}", event.productId(), error);
                        } else {
                            log.info("[KAFKA] low-stock publicado. producto={} stock={} partition={}",
                                    event.productName(), event.currentStock(),
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (RuntimeException ex) {
            log.error("[KAFKA] No se pudo serializar low-stock de {}", event.productId(), ex);
        }
    }
}