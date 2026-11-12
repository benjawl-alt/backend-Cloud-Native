package cl.pedidos360.order.streams;

import cl.pedidos360.order.dto.event.LowStockEvent;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consulta los state stores de Kafka Streams (interactive queries). */
@Service
public class InventoryQueryService {

    private final StreamsBuilderFactoryBean factory;
    private final ObjectMapper mapper;

    public InventoryQueryService(StreamsBuilderFactoryBean factory, ObjectMapper mapper) {
        this.factory = factory;
        this.mapper = mapper;
    }

    public List<Map<String, Object>> topProducts(int limit) {
        ReadOnlyKeyValueStore<String, Long> store = store(InventoryStreamsTopology.UNITS_STORE);
        List<Map<String, Object>> out = new ArrayList<>();
        try (KeyValueIterator<String, Long> it = store.all()) {
            while (it.hasNext()) {
                var kv = it.next();
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("productName", kv.key);
                row.put("unitsSold", kv.value);
                out.add(row);
            }
        } catch (InvalidStateStoreException e) {
            throw unavailable(e);
        }
        out.sort(Comparator.comparing((Map<String, Object> m) -> (Long) m.get("unitsSold")).reversed());
        return out.stream().limit(limit).toList();
    }

    public List<LowStockEvent> lowStockAlerts() {
        ReadOnlyKeyValueStore<String, String> store = store(InventoryStreamsTopology.LOW_STOCK_STORE);
        List<LowStockEvent> out = new ArrayList<>();
        try (KeyValueIterator<String, String> it = store.all()) {
            while (it.hasNext()) {
                String json = it.next().value;
                if (json != null) out.add(mapper.readValue(json, LowStockEvent.class));
            }
        } catch (InvalidStateStoreException e) {
            throw unavailable(e);
        }
        out.sort(Comparator.comparing(LowStockEvent::currentStock));
        return out;
    }

    private <V> ReadOnlyKeyValueStore<String, V> store(String name) {
        KafkaStreams streams = factory.getKafkaStreams();
        if (streams == null) {
            throw unavailable(null);
        }
        try {
            return streams.store(StoreQueryParameters.fromNameAndType(name, QueryableStoreTypes.<String, V>keyValueStore()));
        } catch (InvalidStateStoreException e) {
            throw unavailable(e);
        }
    }

    private ResponseStatusException unavailable(Exception cause) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Kafka Streams aún no está listo, reintenta en unos segundos", cause);
    }
}