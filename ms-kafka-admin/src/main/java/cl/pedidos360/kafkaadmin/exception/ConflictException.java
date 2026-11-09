package cl.pedidos360.kafkaadmin.exception;
public class ConflictException extends RuntimeException {
    public ConflictException(String m) { super(m); }
}