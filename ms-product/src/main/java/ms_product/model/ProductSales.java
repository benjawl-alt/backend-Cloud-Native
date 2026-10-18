package ms_product.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Acumulado de unidades vendidas por producto ("productos más demandados"). No toca la colección products. */
@Document(collection = "product_sales")
public class ProductSales {

    @Id
    private String id;            // productId (o nombre si no viene id)
    private String productName;
    private Long unitsSold;
    private Instant lastSaleAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getUnitsSold() { return unitsSold; }
    public void setUnitsSold(Long unitsSold) { this.unitsSold = unitsSold; }

    public Instant getLastSaleAt() { return lastSaleAt; }
    public void setLastSaleAt(Instant lastSaleAt) { this.lastSaleAt = lastSaleAt; }
}
