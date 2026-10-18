package ms_product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de exchanges, colas y routing keys (definidos solo en application.yml). */
@ConfigurationProperties(prefix = "pedidos360.rabbit")
public record RabbitNamesProperties(Exchanges exchanges, Queues queues, RoutingKeys routingKeys) {

    public record Exchanges(String topic, String dlx) {}

    public record Queues(String sales, String salesDlq) {}

    public record RoutingKeys(String salesBinding) {}
}
