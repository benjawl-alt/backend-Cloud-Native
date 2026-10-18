package cl.pedidos360.rabbitadmin.service;

import cl.pedidos360.rabbitadmin.config.RabbitAdminProperties;
import cl.pedidos360.rabbitadmin.dto.BindingRequest;
import cl.pedidos360.rabbitadmin.dto.ExchangeRequest;
import cl.pedidos360.rabbitadmin.dto.QueueRequest;
import cl.pedidos360.rabbitadmin.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Toda la lógica de administración de RabbitMQ (colas, exchanges y bindings).
 * Los controladores solo llaman a estos métodos y no conocen la librería de RabbitMQ.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;
    private final RabbitManagementClient management;
    private final RabbitAdminProperties props;

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitManagementClient management, RabbitAdminProperties props) {
        this.amqpAdmin = amqpAdmin;
        this.management = management;
        this.props = props;
    }

    // ---------------- Colas ----------------

    public Map<String, Object> createQueue(QueueRequest req) {
        QueueBuilder builder = req.durable() ? QueueBuilder.durable(req.name()) : QueueBuilder.nonDurable(req.name());

        String dlqName = null;
        if (req.withDlq()) {
            dlqName = req.name() + ".dlq";
            DirectExchange dlx = new DirectExchange(props.dlxName(), true, false);
            amqpAdmin.declareExchange(dlx);
            Queue dlq = QueueBuilder.durable(dlqName).build();
            amqpAdmin.declareQueue(dlq);
            amqpAdmin.declareBinding(new Binding(dlqName, Binding.DestinationType.QUEUE,
                    props.dlxName(), dlqName, null));
            builder.deadLetterExchange(props.dlxName()).deadLetterRoutingKey(dlqName);
        }
        if (req.messageTtlMs() != null) builder.ttl(req.messageTtlMs());
        if (req.maxLength() != null) builder.maxLength(req.maxLength());

        amqpAdmin.declareQueue(builder.build());
        log.info("[ADMIN] Cola creada: {} (dlq={})", req.name(), dlqName);

        return Map.of("queue", req.name(), "dlq", dlqName == null ? "" : dlqName, "durable", req.durable());
    }

    public void deleteQueue(String name, boolean withDlq) {
        requireQueue(name);
        amqpAdmin.deleteQueue(name);
        log.info("[ADMIN] Cola eliminada: {}", name);
        if (withDlq && amqpAdmin.getQueueInfo(name + ".dlq") != null) {
            amqpAdmin.deleteQueue(name + ".dlq");
            log.info("[ADMIN] DLQ eliminada: {}.dlq", name);
        }
    }

    public List<Map<String, Object>> listQueues() {
        return management.list("queues",
                List.of("name", "durable", "auto_delete", "arguments", "node", "state", "messages", "consumers"));
    }

    // ---------------- Exchanges ----------------

    public Map<String, Object> createExchange(ExchangeRequest req) {
        Exchange exchange = switch (req.type()) {
            case DIRECT -> new DirectExchange(req.name(), req.durable(), false);
            case TOPIC -> new TopicExchange(req.name(), req.durable(), false);
            case FANOUT -> new FanoutExchange(req.name(), req.durable(), false);
            case HEADERS -> new HeadersExchange(req.name(), req.durable(), false);
        };
        amqpAdmin.declareExchange(exchange);
        log.info("[ADMIN] Exchange creado: {} ({})", req.name(), req.type());
        return Map.of("exchange", req.name(), "type", req.type().name(), "durable", req.durable());
    }

    public void deleteExchange(String name) {
        if (name.startsWith("amq.")) {
            throw new IllegalArgumentException("No se pueden eliminar los exchanges reservados (amq.*)");
        }
        requireExchange(name);
        amqpAdmin.deleteExchange(name);
        log.info("[ADMIN] Exchange eliminado: {}", name);
    }

    public List<Map<String, Object>> listExchanges() {
        return management.list("exchanges", List.of("name", "type", "durable", "auto_delete"));
    }

    // ---------------- Bindings ----------------

    public Map<String, Object> createBinding(BindingRequest req) {
        requireQueue(req.queue());
        requireExchange(req.exchange());
        amqpAdmin.declareBinding(toBinding(req));
        log.info("[ADMIN] Binding creado: {} -[{}]-> {}", req.exchange(), req.routingKey(), req.queue());
        return Map.of("exchange", req.exchange(), "queue", req.queue(), "routingKey", req.routingKey());
    }

    public void deleteBinding(BindingRequest req) {
        requireQueue(req.queue());
        requireExchange(req.exchange());
        amqpAdmin.removeBinding(toBinding(req));
        log.info("[ADMIN] Binding eliminado: {} -[{}]-> {}", req.exchange(), req.routingKey(), req.queue());
    }

    public List<Map<String, Object>> listBindings() {
        return management.list("bindings", List.of("source", "destination", "destination_type", "routing_key"));
    }

    // ---------------- Auxiliares ----------------

    private Binding toBinding(BindingRequest req) {
        return new Binding(req.queue(), Binding.DestinationType.QUEUE, req.exchange(), req.routingKey(), null);
    }

    private void requireQueue(String name) {
        if (amqpAdmin.getQueueInfo(name) == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
    }

    private void requireExchange(String name) {
        if (!management.exchangeExists(name)) {
            throw new ResourceNotFoundException("El exchange '" + name + "' no existe");
        }
    }
}
