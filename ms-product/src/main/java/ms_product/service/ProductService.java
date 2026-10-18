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

import com.mongodb.client.result.UpdateResult;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    public ProductService(ProductRepository productRepository, MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
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
        return productRepository.save(existing);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}