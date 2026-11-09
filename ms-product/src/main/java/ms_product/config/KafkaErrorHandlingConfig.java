package ms_product.config;

import ms_product.messaging.common.InvalidEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {
        DeadLetterPublishingRecoverer dlt = new DeadLetterPublishingRecoverer(template);
        ConsumerRecordRecoverer recoverer = (record, ex) -> {
            log.error("[KAFKA-DLT] topic={} partition={} offset={} motivo={} payload={}",
                    record.topic(), record.partition(), record.offset(), ex.getMessage(), record.value());
            dlt.accept(record, ex);
        };
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
        handler.addNotRetryableExceptions(InvalidEventException.class);
        return handler;
    }
}