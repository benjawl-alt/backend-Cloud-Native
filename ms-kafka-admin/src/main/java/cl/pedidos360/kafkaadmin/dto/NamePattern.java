package cl.pedidos360.kafkaadmin.dto;

public final class NamePattern {
    private NamePattern() {}
    /** Sin espacios, 1-249 caracteres y no puede empezar con "__" (tópicos internos). */
    public static final String TOPIC = "^(?!__)[A-Za-z0-9._-]{1,249}$";
}