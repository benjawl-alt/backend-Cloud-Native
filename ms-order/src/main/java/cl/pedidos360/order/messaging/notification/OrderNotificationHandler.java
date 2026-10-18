package cl.pedidos360.order.messaging.notification;

import cl.pedidos360.order.dto.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Lógica del caso de uso "notificar al cliente" (aislada del consumidor y del broker). */
@Component
public class OrderNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationHandler.class);

    public void handle(OrderCreatedEvent event) {
        // DEMO: simula una falla recuperable (reintenta 1 vez y luego va a la DLQ). Borrar después de la demo.
        if (event.userEmail().startsWith("falla")) {
            throw new IllegalStateException("Fallo simulado del servicio de correo");
        }

        String shortId = event.orderId().length() > 6
                ? event.orderId().substring(event.orderId().length() - 6).toUpperCase()
                : event.orderId();

        log.info("[NOTIFICACION] Email a {}: pedido #{} confirmado por ${} ({} ítems, despacho {})",
                event.userEmail(), shortId, event.totalAmount(), event.items().size(), event.dispatchMethod());
        log.info("[NOTIFICACION] Push web enviada a {}", event.userEmail());
    }
}
