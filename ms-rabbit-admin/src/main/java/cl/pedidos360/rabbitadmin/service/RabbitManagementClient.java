package cl.pedidos360.rabbitadmin.service;

import cl.pedidos360.rabbitadmin.config.RabbitAdminProperties;
import cl.pedidos360.rabbitadmin.exception.ManagementUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lectura (listar / verificar existencia) mediante la API HTTP de gestión de RabbitMQ. */
@Component
public class RabbitManagementClient {

    private final RestClient client;
    private final RabbitAdminProperties props;

    public RabbitManagementClient(RabbitAdminProperties props) {
        this.props = props;
        this.client = RestClient.builder()
                .defaultHeaders(h -> h.setBasicAuth(props.managementUser(), props.managementPassword()))
                .build();
    }

    public List<Map<String, Object>> list(String resource, List<String> fields) {
        URI uri = URI.create(props.managementUrl() + "/api/" + resource + "/%2F");
        try {
            List<Map<String, Object>> raw = client.get().uri(uri).retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            if (raw == null) return List.of();
            return raw.stream().map(m -> pick(m, fields)).toList();
        } catch (ResourceAccessException e) {
            throw new ManagementUnavailableException("No se pudo consultar la API de gestión de RabbitMQ", e);
        }
    }

    public boolean exchangeExists(String name) {
        URI uri = URI.create(props.managementUrl() + "/api/exchanges/%2F/"
                + URLEncoder.encode(name, StandardCharsets.UTF_8));
        try {
            client.get().uri(uri).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (ResourceAccessException e) {
            throw new ManagementUnavailableException("No se pudo consultar la API de gestión de RabbitMQ", e);
        }
    }

    private Map<String, Object> pick(Map<String, Object> source, List<String> fields) {
        Map<String, Object> out = new LinkedHashMap<>();
        fields.forEach(f -> out.put(f, source.get(f)));
        return out;
    }
}
