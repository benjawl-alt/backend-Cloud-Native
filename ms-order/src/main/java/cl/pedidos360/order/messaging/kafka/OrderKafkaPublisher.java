package cl.pedidos360.order.messaging.kafka;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import cl.pedidos360.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public OrderKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.orders-created}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public void publishOrderCreated(Order order) {
        try {
            String payload = objectMapper.writeValueAsString(
                    OrderStreamEvent.from("ORDER_CREATED", order));

            kafkaTemplate.send(topicName, order.getId(), payload)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("[KAFKA] No se pudo publicar el pedido {}", order.getId(), error);
                        } else {
                            log.info("[KAFKA] Evento ORDER_CREATED enviado. pedido={}, topic={}, partition={}",
                                    order.getId(), result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception ex) {
            // No se propaga para no convertir un fallo de streaming en un error HTTP
            // después de que el pedido ya quedó guardado.
            log.error("[KAFKA] No se pudo serializar el pedido {}", order.getId(), ex);
        }
    }
}
