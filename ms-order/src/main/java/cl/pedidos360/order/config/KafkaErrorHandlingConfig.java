package cl.pedidos360.order.config;

import cl.pedidos360.order.messaging.common.InvalidEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Política de errores de los consumidores Kafka:
 *  - payload inválido (InvalidEventException) -> directo a <topic>.DLT, sin reintentos
 *  - falla recuperable -> 2 reintentos (1 s) y luego a <topic>.DLT
 * Siempre se registra en log. Spring Boot aplica este bean a todos los @KafkaListener.
 */
@Configuration
public class KafkaErrorHandlingConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {
        DeadLetterPublishingRecoverer dlt = new DeadLetterPublishingRecoverer(template);

        ConsumerRecordRecoverer recoverer = (record, ex) -> {
            log.error("[KAFKA-DLT] Mensaje enviado a DLT. topic={} partition={} offset={} motivo={} payload={}",
                    record.topic(), record.partition(), record.offset(),
                    ex.getMessage(), record.value());
            dlt.accept(record, ex);
        };

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
        handler.addNotRetryableExceptions(InvalidEventException.class);
        handler.setRetryListeners((record, ex, attempt) ->
                log.warn("[KAFKA-RETRY] intento {} fallido. topic={} offset={} causa={}",
                        attempt, record.topic(), record.offset(), ex.getMessage()));
        return handler;
    }
}