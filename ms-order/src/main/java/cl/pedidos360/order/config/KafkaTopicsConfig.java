package cl.pedidos360.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    private static final String DIAS_7 = String.valueOf(7L * 24 * 60 * 60 * 1000);
    private static final String DIAS_30 = String.valueOf(30L * 24 * 60 * 60 * 1000);

    @Value("${app.kafka.replicas:1}")
    private int replicas;

    /** Eventos de pedidos: se borran a los 7 días (log de eventos). */
    @Bean
    NewTopic ordersCreatedTopic(@Value("${app.kafka.topics.orders-created}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, DIAS_7)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")
                .build();
    }

    /** Alertas de stock: la clave es el productId, por lo que se compacta (queda la última alerta por producto). */
    @Bean
    NewTopic lowStockTopic(@Value("${app.kafka.topics.low-stock}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(replicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG,
                        TopicConfig.CLEANUP_POLICY_COMPACT + "," + TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, DIAS_7)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")
                .build();
    }

    /** DLT: mismas particiones que el origen y retención larga para poder inspeccionarlos. */
    @Bean
    NewTopic ordersCreatedDlt(@Value("${app.kafka.topics.orders-created-dlt}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, DIAS_30).build();
    }

    @Bean
    NewTopic lowStockDlt(@Value("${app.kafka.topics.low-stock-dlt}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, DIAS_30).build();
    }
}