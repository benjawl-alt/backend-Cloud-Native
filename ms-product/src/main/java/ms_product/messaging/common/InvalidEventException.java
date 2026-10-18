package ms_product.messaging.common;

/** Mensaje irrecuperable: va directo a la DLQ. */
public class InvalidEventException extends RuntimeException {
    public InvalidEventException(String message) {
        super(message);
    }
}
