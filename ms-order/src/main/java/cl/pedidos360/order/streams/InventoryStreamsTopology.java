package cl.pedidos360.order.streams;

import cl.pedidos360.order.dto.event.OrderStreamEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso: control concurrente de inventario.
 *  orders.created --(flatMap items)--> unidades vendidas por producto  [units-sold-store]
 *  orders.created (inválidos)       --> orders.created.DLT
 *  low-stock (compactado)           --> última alerta por producto     [low-stock-store]
 */
@Configuration
@EnableKafkaStreams
public class InventoryStreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(InventoryStreamsTopology.class);

    public static final String UNITS_STORE = "units-sold-store";
    public static final String LOW_STOCK_STORE = "low-stock-store";

    @Bean
    public KStream<String, String> inventoryTopology(
            StreamsBuilder builder,
            ObjectMapper mapper,
            @Value("${app.kafka.topics.orders-created}") String ordersTopic,
            @Value("${app.kafka.topics.orders-created-dlt}") String ordersDlt,
            @Value("${app.kafka.topics.low-stock}") String lowStockTopic) {

        KStream<String, String> orders =
                builder.stream(ordersTopic, Consumed.with(Serdes.String(), Serdes.String()));

        // 1) Validación: lo inválido va al DLT con log, y el stream sigue funcionando
        Map<String, KStream<String, String>> branches = orders
                .split(Named.as("orders-"))
                .branch((key, value) -> parse(mapper, value) != null, Branched.as("ok"))
                .defaultBranch(Branched.as("bad"));

        branches.get("orders-bad")
                .peek((key, value) -> log.error("[STREAMS-DLT] Evento inválido enviado a {}. key={} payload={}",
                        ordersDlt, key, value))
                .to(ordersDlt, Produced.with(Serdes.String(), Serdes.String()));

        // 2) Unidades vendidas por producto (se reparticiona por nombre del producto)
        branches.get("orders-ok")
                .flatMap((key, value) -> {
                    List<KeyValue<String, Long>> out = new ArrayList<>();
                    OrderStreamEvent event = parse(mapper, value);
                    if (event != null) {
                        for (OrderStreamEvent.Item item : event.items()) {
                            if (item.productName() != null && item.quantity() != null) {
                                out.add(KeyValue.pair(item.productName(), item.quantity().longValue()));
                            }
                        }
                    }
                    return out;
                })
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .reduce(Long::sum,
                        Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(UNITS_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()));

        // 3) Tabla con la última alerta de stock bajo por producto
        builder.table(lowStockTopic,
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(LOW_STOCK_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.String()));

        return orders;
    }

    private static OrderStreamEvent parse(ObjectMapper mapper, String value) {
        try {
            OrderStreamEvent event = mapper.readValue(value, OrderStreamEvent.class);
            boolean valid = event != null && event.orderId() != null
                    && event.items() != null && !event.items().isEmpty();
            return valid ? event : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }
}