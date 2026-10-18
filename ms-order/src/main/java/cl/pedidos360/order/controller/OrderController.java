package cl.pedidos360.order.controller;

import cl.pedidos360.order.messaging.service.OrderMessagingService;   // <-- NUEVO
import cl.pedidos360.order.messaging.kafka.OrderKafkaPublisher;
import cl.pedidos360.order.model.Order;
import cl.pedidos360.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired                                                         // <-- NUEVO
    private OrderMessagingService orderMessagingService;               // <-- NUEVO

    @Autowired
    private OrderKafkaPublisher orderKafkaPublisher;

    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody Order orderRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String userEmail = resolveEmail(jwt);

        // Validar que vengan ítems con productId
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        for (Order.OrderItem i : orderRequest.getItems()) {
            if (i.getProductId() == null || i.getQuantity() == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        // 1) Descontar stock en ms-product
        List<Map<String, Object>> stockItems = orderRequest.getItems().stream()
                .map(i -> Map.<String, Object>of(
                        "productId", i.getProductId(),
                        "quantity", i.getQuantity()))
                .toList();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(
                    productServiceUrl + "/api/products/decrease-stock",
                    new HttpEntity<>(stockItems, headers),
                    Void.class);
        } catch (HttpClientErrorException e) {
            System.out.println(">>> ms-product respondió con error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build(); // 409 si no hay stock
        } catch (RestClientException e) {
            System.out.println(">>> Error llamando a ms-product (" + productServiceUrl + "): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        // 2) Guardar el pedido SIEMPRE con el usuario del token (nunca el que venga en el body)
        orderRequest.setId(null);
        orderRequest.setUserEmail(userEmail);
        orderRequest.setStatus("CREADO");
        orderRequest.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(orderRequest);
        // Kafka: evento de streaming/auditoría. RabbitMQ permanece intacto abajo.
        orderKafkaPublisher.publishOrderCreated(savedOrder);
        System.out.println(">>> Pedido guardado en MongoDB con éxito para: " + userEmail);

        // 3) NUEVO: publicar en RabbitMQ (notificación, ticket de cocina, ventas). Si el broker falla, el pedido igual queda guardado.
        orderMessagingService.publishOrderCreated(savedOrder);

        return ResponseEntity.ok(savedOrder);
    }

    // Mis pedidos: solo los del usuario logueado
    @GetMapping("/mine")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                orderRepository.findByUserEmailOrderByCreatedAtDesc(resolveEmail(jwt)));
    }

    // Todos los pedidos: solo administradores
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Order> orders = orderRepository.findAll();
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return ResponseEntity.ok(orders);
    }

    private String resolveEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("preferred_username");
        if (email == null) email = jwt.getClaimAsString("upn");
        if (email == null) email = jwt.getSubject();
        return email;
    }

    private boolean isAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && (roles.contains("Admin")
                || roles.contains("Administrator")
                || roles.contains("Administrador"));
    }
}
