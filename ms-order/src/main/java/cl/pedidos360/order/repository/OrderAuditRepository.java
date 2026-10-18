package cl.pedidos360.order.repository;

import cl.pedidos360.order.model.OrderAudit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderAuditRepository extends MongoRepository<OrderAudit, String> {
}
