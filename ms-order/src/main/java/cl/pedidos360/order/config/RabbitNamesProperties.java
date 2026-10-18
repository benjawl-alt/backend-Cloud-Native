package cl.pedidos360.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de exchanges, colas y routing keys leídos de application.yml (único lugar donde se definen). */
@ConfigurationProperties(prefix = "pedidos360.rabbit")
public record RabbitNamesProperties(
        Exchanges exchanges,
        Queues queues,
        RoutingKeys routingKeys) {

    public record Exchanges(String topic, String direct, String dlx) {}

    public record Queues(String notification, String notificationDlq, String kitchen, String kitchenDlq) {}

    public record RoutingKeys(String orderCreated, String notificationPattern, String kitchenTicket) {}
}
