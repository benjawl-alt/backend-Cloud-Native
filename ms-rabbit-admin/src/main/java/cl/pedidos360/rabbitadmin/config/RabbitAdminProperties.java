package cl.pedidos360.rabbitadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbit-admin")
public record RabbitAdminProperties(
        String managementUrl,
        String managementUser,
        String managementPassword,
        String dlxName) {}
