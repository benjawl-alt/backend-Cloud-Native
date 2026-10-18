package ms_product.config;

import ms_product.model.Product;
import ms_product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class DataInitializer {

    // Clave: nombre del producto en minúsculas. Valor: ruta de la foto en public/images/productos/
    private static final Map<String, String> IMAGENES = Map.ofEntries(
        Map.entry("espresso", "/images/productos/espresso.jpg"),
        Map.entry("americano", "/images/productos/americano.jpg"),
        Map.entry("cappuccino", "/images/productos/cappuccino.jpg"),
        Map.entry("latte", "/images/productos/latte.jpg"),
        Map.entry("mocaccino", "/images/productos/mocaccino.jpg"),
        Map.entry("chocolate caliente", "/images/productos/chocolate-caliente.jpg"),
        Map.entry("jugo natural", "/images/productos/jugo-natural.jpg"),
        Map.entry("croissant de mantequilla", "/images/productos/croissant-mantequilla.jpg"),
        Map.entry("empanada de queso", "/images/productos/empanada-queso.jpg"),
        Map.entry("tostada con palta", "/images/productos/tostada-palta.jpg"),
        Map.entry("sándwich ave palta", "/images/productos/sandwich-ave-palta.jpg"),
        Map.entry("sándwich barros luco", "/images/productos/sandwich-barros-luco.jpg"),
        Map.entry("kuchen de frambuesa", "/images/productos/kuchen-frambuesa.jpg"),
        Map.entry("brownie", "/images/productos/brownie.jpg"),
        Map.entry("alfajor", "/images/productos/alfajor.jpg")
    );

    private static String imagenPara(String nombre) {
        return nombre == null ? null : IMAGENES.get(nombre.trim().toLowerCase());
    }

    private static Product nuevo(String nombre, String descripcion, double precio, int stock) {
        return new Product(nombre, descripcion, precio, stock, imagenPara(nombre));
    }

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(nuevo("Espresso", "Shot de café de origen, intenso y aromático", 1600.0, 99));
                productRepository.save(nuevo("Americano", "Espresso alargado con agua caliente", 2200.0, 99));
                productRepository.save(nuevo("Cappuccino", "Espresso con leche vaporizada y espuma cremosa", 2900.0, 99));
                productRepository.save(nuevo("Latte", "Espresso con abundante leche y una capa de espuma", 3200.0, 100));
                productRepository.save(nuevo("Mocaccino", "Espresso, chocolate y leche vaporizada", 3500.0, 80));
                productRepository.save(nuevo("Chocolate caliente", "Chocolate con leche caliente y crema", 3000.0, 60));
                productRepository.save(nuevo("Jugo natural", "Jugo de fruta de temporada exprimido al momento", 2800.0, 50));
                productRepository.save(nuevo("Croissant de mantequilla", "Croissant hojaldrado recién horneado", 1900.0, 40));
                productRepository.save(nuevo("Empanada de queso", "Empanada frita rellena de queso derretido", 2500.0, 40));
                productRepository.save(nuevo("Tostada con palta", "Pan de masa madre con palta, limón y sal de mar", 3900.0, 30));
                productRepository.save(nuevo("Sándwich ave palta", "Pechuga de ave, palta y mayonesa en pan fresco", 4500.0, 30));
                productRepository.save(nuevo("Sándwich barros luco", "Lomo de res y queso derretido en pan frica", 5200.0, 25));
                productRepository.save(nuevo("Kuchen de frambuesa", "Kuchen casero con frambuesas y crema pastelera", 3200.0, 20));
                productRepository.save(nuevo("Brownie", "Brownie de chocolate semiamargo con nueces", 2600.0, 35));
                productRepository.save(nuevo("Alfajor", "Alfajor de manjar bañado en chocolate", 1500.0, 50));
                System.out.println(">>> Productos iniciales cargados en pedidos360_product exitosamente.");
            }

            // Asigna foto a los productos existentes que todavía no tienen
            productRepository.findAll().forEach(p -> {
                boolean sinImagen = p.getImageUrl() == null || p.getImageUrl().isBlank();
                String ruta = imagenPara(p.getName());
                if (sinImagen && ruta != null) {
                    p.setImageUrl(ruta);
                    productRepository.save(p);
                }
            });
        };
    }
}