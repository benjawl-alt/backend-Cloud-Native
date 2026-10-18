package cl.pedidos360.rabbitadmin.controller;

import cl.pedidos360.rabbitadmin.dto.BindingRequest;
import cl.pedidos360.rabbitadmin.dto.ExchangeRequest;
import cl.pedidos360.rabbitadmin.dto.NamePattern;
import cl.pedidos360.rabbitadmin.dto.QueueRequest;
import cl.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API REST del microservicio administrador de RabbitMQ.
 *
 *  POST   /api/rabbit/queues                 crea cola (y su DLQ)     GET    /api/rabbit/queues
 *  DELETE /api/rabbit/queues/{name}          elimina cola
 *  POST   /api/rabbit/exchanges              crea exchange            GET    /api/rabbit/exchanges
 *  DELETE /api/rabbit/exchanges/{name}       elimina exchange
 *  POST   /api/rabbit/bindings               crea binding             GET    /api/rabbit/bindings
 *  DELETE /api/rabbit/bindings?exchange=&queue=&routingKey=   elimina binding
 */
@RestController
@RequestMapping("/api/rabbit")
@Validated
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // ---------- Queues ----------

    @PostMapping("/queues")
    public ResponseEntity<Map<String, Object>> createQueue(@Valid @RequestBody QueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQueue(request));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(
            @PathVariable @Pattern(regexp = NamePattern.NAME, message = "Nombre de cola inválido") String name,
            @RequestParam(defaultValue = "false") boolean withDlq) {
        service.deleteQueue(name, withDlq);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/queues")
    public List<Map<String, Object>> listQueues() {
        return service.listQueues();
    }

    // ---------- Exchanges ----------

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, Object>> createExchange(@Valid @RequestBody ExchangeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createExchange(request));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(
            @PathVariable @Pattern(regexp = NamePattern.NAME, message = "Nombre de exchange inválido") String name) {
        service.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exchanges")
    public List<Map<String, Object>> listExchanges() {
        return service.listExchanges();
    }

    // ---------- Bindings ----------

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, Object>> createBinding(@Valid @RequestBody BindingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBinding(request));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(
            @RequestParam @NotBlank @Pattern(regexp = NamePattern.NAME, message = "Nombre de exchange inválido") String exchange,
            @RequestParam @NotBlank @Pattern(regexp = NamePattern.NAME, message = "Nombre de cola inválido") String queue,
            @RequestParam(defaultValue = "") @Pattern(regexp = NamePattern.ROUTING_KEY, message = "Routing key inválida") String routingKey) {
        service.deleteBinding(new BindingRequest(exchange, queue, routingKey));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bindings")
    public List<Map<String, Object>> listBindings() {
        return service.listBindings();
    }
}
