package cl.pedidos360.kafkaadmin.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String m) { super(m); }
}