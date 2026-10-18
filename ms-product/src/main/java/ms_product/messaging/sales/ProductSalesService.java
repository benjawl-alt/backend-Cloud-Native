package ms_product.messaging.sales;

import ms_product.dto.event.OrderCreatedEvent;
import ms_product.model.ProcessedOrderEvent;
import ms_product.model.ProductSales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Caso de uso: registrar unidades vendidas por producto a partir del evento order.created. */
@Service
public class ProductSalesService {

    private static final Logger log = LoggerFactory.getLogger(ProductSalesService.class);

    private final MongoTemplate mongo;

    public ProductSalesService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public void register(OrderCreatedEvent event) {
        try {
            mongo.insert(new ProcessedOrderEvent(event.orderId(), Instant.now()));
        } catch (DuplicateKeyException dup) {
            log.warn("[VENTAS] Pedido {} ya procesado, se ignora (idempotencia)", event.orderId());
            return;
        }

        try {
            for (OrderCreatedEvent.Item item : event.items()) {
                String key = (item.productId() != null && !item.productId().isBlank())
                        ? item.productId() : item.productName();
                Update update = new Update()
                        .inc("unitsSold", item.quantity())
                        .set("productName", item.productName())
                        .set("lastSaleAt", Instant.now());
                mongo.upsert(Query.query(Criteria.where("_id").is(key)), update, ProductSales.class);
            }
            log.info("[VENTAS] Pedido {} registrado ({} productos)", event.orderId(), event.items().size());
        } catch (RuntimeException ex) {
            // se libera la marca para que el reintento pueda procesarlo
            mongo.remove(Query.query(Criteria.where("_id").is(event.orderId())), ProcessedOrderEvent.class);
            throw ex;
        }
    }
}
