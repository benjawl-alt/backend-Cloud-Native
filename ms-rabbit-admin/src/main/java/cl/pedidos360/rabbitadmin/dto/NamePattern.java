package cl.pedidos360.rabbitadmin.dto;

/** Expresiones regulares compartidas por las validaciones. */
public final class NamePattern {
    private NamePattern() {}

    /** Nombres de colas/exchanges: sin espacios, 1-100 caracteres, y no pueden empezar con "amq." (reservado). */
    public static final String NAME = "^(?!amq\\.)[A-Za-z0-9._-]{1,100}$";

    /** Routing keys / binding keys (permiten * y # de topic). Puede ir vacía (fanout). */
    public static final String ROUTING_KEY = "^[A-Za-z0-9._*#-]{0,255}$";
}
