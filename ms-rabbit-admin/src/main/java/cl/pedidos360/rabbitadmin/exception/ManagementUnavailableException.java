package cl.pedidos360.rabbitadmin.exception;

public class ManagementUnavailableException extends RuntimeException {
    public ManagementUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
