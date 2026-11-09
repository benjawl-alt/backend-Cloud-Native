package cl.pedidos360.kafkaadmin.service;

import cl.pedidos360.kafkaadmin.config.KafkaAdminProperties;
import cl.pedidos360.kafkaadmin.dto.TopicRequest;
import cl.pedidos360.kafkaadmin.dto.TopicUpdateRequest;
import cl.pedidos360.kafkaadmin.exception.ConflictException;
import cl.pedidos360.kafkaadmin.exception.KafkaUnavailableException;
import cl.pedidos360.kafkaadmin.exception.ResourceNotFoundException;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/** Toda la administración de Kafka (tópicos, configuraciones y consumer groups). */
@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);

    private final Admin admin;
    private final int timeout;

    public KafkaAdminService(Admin admin, KafkaAdminProperties props) {
        this.admin = admin;
        this.timeout = props.timeoutSeconds();
    }

    // ===================== Tópicos =====================

    public Map<String, Object> createTopic(TopicRequest req) {
        NewTopic topic = new NewTopic(req.name(), req.partitions(), req.replicationFactor().shortValue());
        if (req.configs() != null && !req.configs().isEmpty()) topic.configs(req.configs());
        await(admin.createTopics(List.of(topic)).all());
        log.info("[ADMIN] Tópico creado: {} (p={}, rf={}, configs={})",
                req.name(), req.partitions(), req.replicationFactor(), req.configs());
        return describeTopic(req.name());
    }

    public List<Map<String, Object>> listTopics() {
        Set<String> names = await(admin.listTopics(new ListTopicsOptions().listInternal(false)).names());
        if (names.isEmpty()) return List.of();
        Map<String, TopicDescription> described = await(admin.describeTopics(names).allTopicNames());
        return described.values().stream()
                .sorted(Comparator.comparing(TopicDescription::name))
                .map(this::summary)
                .toList();
    }

    public Map<String, Object> describeTopic(String name) {
        TopicDescription d = await(admin.describeTopics(List.of(name)).allTopicNames()).get(name);
        Map<String, Object> out = new LinkedHashMap<>(summary(d));
        out.put("configs", topicOverrides(name));
        out.put("partitionDetails", d.partitions().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("partition", p.partition());
            m.put("leader", p.leader() == null ? null : p.leader().id());
            m.put("replicas", p.replicas().stream().map(n -> n.id()).toList());
            m.put("isr", p.isr().stream().map(n -> n.id()).toList());
            return m;
        }).toList());
        return out;
    }

    /** Aumenta particiones y/o cambia configuraciones (retention.ms, cleanup.policy, etc.). */
    public Map<String, Object> updateTopic(String name, TopicUpdateRequest req) {
        int current = await(admin.describeTopics(List.of(name)).allTopicNames()).get(name).partitions().size();

        if (req.partitions() == null && (req.configs() == null || req.configs().isEmpty())) {
            throw new IllegalArgumentException("Debes indicar 'partitions' y/o 'configs'");
        }
        if (req.partitions() != null) {
            if (req.partitions() <= current) {
                throw new IllegalArgumentException(
                        "Solo se pueden aumentar particiones (actual=" + current + ", pedido=" + req.partitions() + ")");
            }
            await(admin.createPartitions(Map.of(name, NewPartitions.increaseTo(req.partitions()))).all());
            log.info("[ADMIN] Tópico {}: particiones {} -> {}", name, current, req.partitions());
        }
        if (req.configs() != null && !req.configs().isEmpty()) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
            Collection<AlterConfigOp> ops = req.configs().entrySet().stream()
                    .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                    .toList();
            await(admin.incrementalAlterConfigs(Map.of(resource, ops)).all());
            log.info("[ADMIN] Tópico {}: configs actualizadas {}", name, req.configs());
        }
        return describeTopic(name);
    }

    public void deleteTopic(String name) {
        await(admin.describeTopics(List.of(name)).allTopicNames()); // 404 si no existe
        await(admin.deleteTopics(List.of(name)).all());
        log.info("[ADMIN] Tópico eliminado: {}", name);
    }

    // ===================== Consumer groups =====================

    public List<Map<String, Object>> listConsumerGroups() {
        return await(admin.listConsumerGroups().all()).stream()
                .sorted(Comparator.comparing(ConsumerGroupListing::groupId))
                .map(g -> Map.<String, Object>of("groupId", g.groupId()))
                .toList();
    }

    /** Miembros, particiones asignadas y lag (offset final - offset confirmado) por partición. */
    public Map<String, Object> describeConsumerGroup(String groupId) {
        ConsumerGroupDescription d = await(admin.describeConsumerGroups(List.of(groupId)).all()).get(groupId);
        Map<TopicPartition, OffsetAndMetadata> committed =
                await(admin.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata());

        if (d.members().isEmpty() && committed.isEmpty()) {
            throw new ResourceNotFoundException("El consumer group '" + groupId + "' no existe");
        }

        Map<TopicPartition, OffsetSpec> latestReq = committed.keySet().stream()
                .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest =
                latestReq.isEmpty() ? Map.of() : await(admin.listOffsets(latestReq).all());

        List<Map<String, Object>> partitions = new ArrayList<>();
        long totalLag = 0;
        for (var e : committed.entrySet()) {
            long end = latest.get(e.getKey()).offset();
            long lag = Math.max(0, end - e.getValue().offset());
            totalLag += lag;
            partitions.add(Map.of("topic", e.getKey().topic(), "partition", e.getKey().partition(),
                    "committedOffset", e.getValue().offset(), "endOffset", end, "lag", lag));
        }
        partitions.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("topic"))
                .thenComparing(m -> (Integer) m.get("partition")));

        List<Map<String, Object>> members = d.members().stream().map(m -> Map.<String, Object>of(
                "memberId", m.consumerId(),
                "clientId", m.clientId(),
                "host", m.host(),
                "assignment", m.assignment().topicPartitions().stream().map(TopicPartition::toString).sorted().toList()
        )).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("groupId", groupId);
        out.put("members", members);
        out.put("totalLag", totalLag);
        out.put("partitions", partitions);
        return out;
    }

    /** Solo se puede borrar un grupo sin miembros activos. */
    public void deleteConsumerGroup(String groupId) {
        await(admin.deleteConsumerGroups(List.of(groupId)).all());
        log.info("[ADMIN] Consumer group eliminado: {}", groupId);
    }

    // ===================== Auxiliares =====================

    private Map<String, Object> summary(TopicDescription d) {
        int rf = d.partitions().isEmpty() ? 0 : d.partitions().get(0).replicas().size();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", d.name());
        m.put("partitions", d.partitions().size());
        m.put("replicationFactor", rf);
        return m;
    }

    /** Solo las configuraciones sobreescritas explícitamente en el tópico. */
    private Map<String, String> topicOverrides(String topic) {
        ConfigResource res = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        Config cfg = await(admin.describeConfigs(List.of(res)).all()).get(res);
        Map<String, String> out = new TreeMap<>();
        cfg.entries().stream()
                .filter(e -> e.source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG)
                .forEach(e -> out.put(e.name(), e.value()));
        return out;
    }

    private <T> T await(KafkaFuture<T> future) {
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaUnavailableException("Operación interrumpida", e);
        } catch (TimeoutException e) {
            throw new KafkaUnavailableException("Kafka no respondió a tiempo", e);
        } catch (ExecutionException e) {
            throw translate(e.getCause());
        }
    }

    private RuntimeException translate(Throwable c) {
        if (c instanceof TopicExistsException) return new ConflictException("El tópico ya existe");
        if (c instanceof GroupNotEmptyException) return new ConflictException("El grupo tiene miembros activos");
        if (c instanceof UnknownTopicOrPartitionException) return new ResourceNotFoundException("El tópico no existe");
        if (c instanceof GroupIdNotFoundException) return new ResourceNotFoundException("El consumer group no existe");
        if (c instanceof InvalidTopicException || c instanceof InvalidPartitionsException
                || c instanceof InvalidReplicationFactorException || c instanceof InvalidConfigurationException
                || c instanceof InvalidRequestException) {
            return new IllegalArgumentException(c.getMessage());
        }
        return new KafkaUnavailableException("Error consultando Kafka: " + c.getMessage(), c);
    }
}