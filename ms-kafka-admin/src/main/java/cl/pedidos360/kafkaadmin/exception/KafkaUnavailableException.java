package cl.pedidos360.kafkaadmin.exception;
public class KafkaUnavailableException extends RuntimeException {
    public KafkaUnavailableException(String m, Throwable c) { super(m, c); }
}