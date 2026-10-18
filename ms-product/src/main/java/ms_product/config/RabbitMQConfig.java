package ms_product.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de ms-product:
 *  pedidos360.topic --order.created--> product.sales.queue --(DLX)--> product.sales.dlq
 * (los exchanges se declaran igual que en ms-order: declarar dos veces lo mismo es idempotente)
 */
@Configuration
@EnableConfigurationProperties(RabbitNamesProperties.class)
public class RabbitMQConfig {

    private final RabbitNamesProperties names;

    public RabbitMQConfig(RabbitNamesProperties names) {
        this.names = names;
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(names.exchanges().topic(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(names.exchanges().dlx(), true, false);
    }

    @Bean
    public Queue salesQueue() {
        return QueueBuilder.durable(names.queues().sales())
                .deadLetterExchange(names.exchanges().dlx())
                .deadLetterRoutingKey(names.queues().salesDlq())
                .build();
    }

    @Bean
    public Queue salesDlq() {
        return QueueBuilder.durable(names.queues().salesDlq()).build();
    }

    @Bean
    public Binding salesBinding() {
        return BindingBuilder.bind(salesQueue()).to(topicExchange())
                .with(names.routingKeys().salesBinding());
    }

    @Bean
    public Binding salesDlqBinding() {
        return BindingBuilder.bind(salesDlq()).to(deadLetterExchange())
                .with(names.queues().salesDlq());
    }
}
