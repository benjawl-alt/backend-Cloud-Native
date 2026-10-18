# Pedidos360 — Kafka + Zookeeper integrado

Este paquete contiene el backend original y una integración inicial de Kafka/Zookeeper. Se conservaron los servicios y el flujo RabbitMQ existente; se añadieron Kafka, Zookeeper, Kafka UI, el publisher, consumidores de auditoría/analítica y el controlador de streaming.

## Levantar desde esta carpeta

Abra PowerShell en `proyecto-pedidos360` y ejecute:

```powershell
docker compose config
docker compose up -d --build
docker compose ps
```

Kafka UI está enlazada a localhost: `http://localhost:8090`. El puerto externo Kafka `29092` y Zookeeper `2181` también se enlazan solo a localhost.

## Comprobación

```powershell
docker exec pedidos360-kafka kafka-topics --bootstrap-server kafka:9092 --list
docker logs pedidos360-ms-order --tail 100
docker exec pedidos360-kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic pedidos360.orders.created --from-beginning --max-messages 1
```

El evento se publica en paralelo al flujo RabbitMQ que ya existía. La auditoría se guarda en la colección MongoDB `order_audit`. Las métricas de `/api/orders/stream/metrics` son contadores en memoria y se reinician si se reinicia `ms-order`. Las rutas del controlador siguen requiriendo JWT según la configuración de seguridad existente.

## Importante

- No ejecutar `docker compose down -v`: eliminaría los volúmenes de datos.
- Esta integración inicial no usa patrón outbox; si Kafka está caído después de guardar el pedido, el pedido puede quedar guardado sin que su evento se publique.
- El código se integró estáticamente, pero debe compilarse y probarse en tu equipo antes de usarlo en la evaluación.
