package cl.pedidos360.order.repository;

import cl.pedidos360.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}