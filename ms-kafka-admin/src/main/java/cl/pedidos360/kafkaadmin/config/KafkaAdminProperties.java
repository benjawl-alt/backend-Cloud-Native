package cl.pedidos360.kafkaadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka-admin")
public record KafkaAdminProperties(
        String bootstrapServers,
        int timeoutSeconds
) {}