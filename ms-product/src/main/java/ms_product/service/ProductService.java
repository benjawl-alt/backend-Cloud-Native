
package ms_product.service;

import ms_product.dto.StockItem;
import ms_product.model.Product;
import ms_product.repository.ProductRepository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ms_product.dto.event.LowStockEvent;
import ms_product.messaging.kafka.LowStockKafkaPublisher;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.result.UpdateResult;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final LowStockKafkaPublisher lowStockPublisher;
    private final int lowStockThreshold;

    public ProductService(ProductRepository productRepository, MongoTemplate mongoTemplate,
                          LowStockKafkaPublisher lowStockPublisher,
                          @Value("${app.stock.low-threshold:10}") int lowStockThreshold) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.lowStockPublisher = lowStockPublisher;
        this.lowStockThreshold = lowStockThreshold;
    }

    public void decreaseStock(List<StockItem> items) {
        List<StockItem> applied = new ArrayList<>();

        for (StockItem item : items) {
            // Solo descuenta si hay stock suficiente (operación atómica)
            Query query = new Query(Criteria.where("id").is(item.productId())
                    .and("stock").gte(item.quantity()));
            Update update = new Update().inc("stock", -item.quantity());

            UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);

            if (result.getModifiedCount() == 0) {
                // Revertir lo ya descontado en este pedido
                for (StockItem done : applied) {
                    mongoTemplate.updateFirst(
                            new Query(Criteria.where("id").is(done.productId())),
                            new Update().inc("stock", done.quantity()),
                            Product.class);
                }
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para el producto " + item.productId());
            }
            applied.add(item);
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product data) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        existing.setName(data.getName());
        existing.setDescription(data.getDescription());
        existing.setPrice(data.getPrice());
        existing.setStock(data.getStock());

        if (data.getImageUrl() != null) {
            existing.setImageUrl(data.getImageUrl());
        }

        Product saved = productRepository.save(existing);

        // Sincronizar alerta Kafka según el stock actualizado
        syncLowStockAlert(saved);

        return saved;
    }

    /** Mantiene el estado de la alerta coherente con el stock actual. */
    private void syncLowStockAlert(Product p) {
        if (p.getStock() == null) return;

        if (p.getStock() <= lowStockThreshold) {
            lowStockPublisher.publish(new LowStockEvent(
                    p.getId(),
                    p.getName(),
                    p.getStock(),
                    lowStockThreshold,
                    Instant.now()));
        } else {
            lowStockPublisher.clear(p.getId());
        }
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);

        // Eliminar también la alerta Kafka del producto eliminado
        lowStockPublisher.clear(id);
    }
}
