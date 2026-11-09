package cl.pedidos360.kafkaadmin.controller;

import cl.pedidos360.kafkaadmin.dto.NamePattern;
import cl.pedidos360.kafkaadmin.dto.TopicRequest;
import cl.pedidos360.kafkaadmin.dto.TopicUpdateRequest;
import cl.pedidos360.kafkaadmin.service.KafkaAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *  POST   /api/kafka/topics                     crea tópico
 *  GET    /api/kafka/topics                     lista tópicos
 *  GET    /api/kafka/topics/{name}              detalle (particiones, ISR, configs)
 *  PUT    /api/kafka/topics/{name}              +particiones y/o configs
 *  DELETE /api/kafka/topics/{name}              elimina tópico
 *  GET    /api/kafka/consumer-groups            lista grupos
 *  GET    /api/kafka/consumer-groups/{id}       miembros, offsets y lag
 *  DELETE /api/kafka/consumer-groups/{id}       elimina grupo (sin miembros)
 */
@RestController
@RequestMapping("/api/kafka")
@Validated
public class KafkaAdminController {

    private final KafkaAdminService service;

    public KafkaAdminController(KafkaAdminService service) {
        this.service = service;
    }

    @PostMapping("/topics")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTopic(request));
    }

    @GetMapping("/topics")
    public List<Map<String, Object>> list() {
        return service.listTopics();
    }

    @GetMapping("/topics/{name}")
    public Map<String, Object> get(
            @PathVariable @Pattern(regexp = NamePattern.TOPIC, message = "Nombre de tópico inválido") String name) {
        return service.describeTopic(name);
    }

    @PutMapping("/topics/{name}")
    public Map<String, Object> update(
            @PathVariable @Pattern(regexp = NamePattern.TOPIC, message = "Nombre de tópico inválido") String name,
            @Valid @RequestBody TopicUpdateRequest request) {
        return service.updateTopic(name, request);
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Void> delete(
            @PathVariable @Pattern(regexp = NamePattern.TOPIC, message = "Nombre de tópico inválido") String name) {
        service.deleteTopic(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer-groups")
    public List<Map<String, Object>> groups() {
        return service.listConsumerGroups();
    }

    @GetMapping("/consumer-groups/{id}")
    public Map<String, Object> group(@PathVariable String id) {
        return service.describeConsumerGroup(id);
    }

    @DeleteMapping("/consumer-groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        service.deleteConsumerGroup(id);
        return ResponseEntity.noContent().build();
    }
}