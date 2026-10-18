package cl.pedidos360.order.messaging.kitchen;

import cl.pedidos360.order.dto.event.KitchenTicketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Lógica del caso de uso "generar ticket de cocina" (aquí se imprime en log; puede persistirse o ir a un KDS). */
@Component
public class KitchenTicketHandler {

    private static final Logger log = LoggerFactory.getLogger(KitchenTicketHandler.class);

    public void handle(KitchenTicketMessage ticket) {
        StringBuilder sb = new StringBuilder("\n====== COMANDA COCINA ======\n");
        sb.append("Pedido: ").append(ticket.orderId()).append('\n');
        sb.append("Despacho: ").append(ticket.dispatchMethod()).append('\n');
        ticket.lines().forEach(l -> sb.append(" - ").append(l.quantity()).append(" x ").append(l.productName()).append('\n'));
        sb.append("============================");
        log.info("[COCINA] {}", sb);
    }
}
