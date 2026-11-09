package cl.pedidos360.kafkaadmin.config;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaAdminProperties.class)
public class KafkaAdminConfig {

    @Bean(destroyMethod = "close")
    public Admin kafkaAdminClient(KafkaAdminProperties props) {

        return Admin.create(Map.<String, Object>of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                props.bootstrapServers(),

                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,
                props.timeoutSeconds() * 1000,

                AdminClientConfig.CLIENT_ID_CONFIG,
                "ms-kafka-admin"
        ));
    }
}