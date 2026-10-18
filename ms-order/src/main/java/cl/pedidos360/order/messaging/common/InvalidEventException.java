package cl.pedidos360.order.messaging.common;

/** Mensaje irrecuperable (JSON roto o DTO inválido): va directo a la DLQ, sin reintentos. */
public class InvalidEventException extends RuntimeException {
    public InvalidEventException(String message) {
        super(message);
    }
}
