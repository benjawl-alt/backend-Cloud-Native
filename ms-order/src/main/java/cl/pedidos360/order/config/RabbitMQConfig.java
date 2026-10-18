package cl.pedidos360.order.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Toda la topología de RabbitMQ de ms-order (exchanges, colas, DLQ y bindings).
 * La lógica de negocio NO declara nada de RabbitMQ.
 *
 *  pedidos360.topic  (topic)  --order.*-->        order.notification.queue  --(DLX)--> order.notification.dlq
 *  pedidos360.direct (direct) --kitchen.ticket--> kitchen.ticket.queue      --(DLX)--> kitchen.ticket.dlq
 *  pedidos360.dlx    (direct) recibe los mensajes rechazados y los deriva a cada DLQ
 */
@Configuration
@EnableConfigurationProperties(RabbitNamesProperties.class)
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    private final RabbitNamesProperties names;

    public RabbitMQConfig(RabbitNamesProperties names) {
        this.names = names;
    }

    // ---------------- Exchanges ----------------

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(names.exchanges().topic(), true, false);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(names.exchanges().direct(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(names.exchanges().dlx(), true, false);
    }

    // ---------------- Caso de uso: notificaciones ----------------

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(names.queues().notification())
                .deadLetterExchange(names.exchanges().dlx())
                .deadLetterRoutingKey(names.queues().notificationDlq())
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(names.queues().notificationDlq()).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(topicExchange())
                .with(names.routingKeys().notificationPattern());
    }

    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq()).to(deadLetterExchange())
                .with(names.queues().notificationDlq());
    }

    // ---------------- Caso de uso: ticket de cocina ----------------

    @Bean
    public Queue kitchenQueue() {
        return QueueBuilder.durable(names.queues().kitchen())
                .deadLetterExchange(names.exchanges().dlx())
                .deadLetterRoutingKey(names.queues().kitchenDlq())
                .build();
    }

    @Bean
    public Queue kitchenDlq() {
        return QueueBuilder.durable(names.queues().kitchenDlq()).build();
    }

    @Bean
    public Binding kitchenBinding() {
        return BindingBuilder.bind(kitchenQueue()).to(directExchange())
                .with(names.routingKeys().kitchenTicket());
    }

    @Bean
    public Binding kitchenDlqBinding() {
        return BindingBuilder.bind(kitchenDlq()).to(deadLetterExchange())
                .with(names.queues().kitchenDlq());
    }

    // ---------------- Plantilla de envío (confirms + returns). El JSON lo arma JsonMessageSupport ----------------

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        template.setConfirmCallback((correlation, ack, cause) -> {
            if (!ack) {
                log.error("[PRODUCER] El broker NO confirmó el mensaje id={} causa={}",
                        correlation != null ? correlation.getId() : "?", cause);
            }
        });
        template.setReturnsCallback(returned -> log.error(
                "[PRODUCER] Mensaje sin cola destino (no ruteable). exchange={} routingKey={} motivo={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
        return template;
    }
}
