package com.valleblanco.backend.config;

import com.valleblanco.backend.model.NotaCata;
import com.valleblanco.backend.model.Precio;
import com.valleblanco.backend.model.Product;
import com.valleblanco.backend.repository.NotaCataRepository;
import com.valleblanco.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;
    private final NotaCataRepository notaCataRepository;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            NotaCata n1 = getOrCreateNota("Chocolate amargo");
            NotaCata n2 = getOrCreateNota("Naranja");
            NotaCata n3 = getOrCreateNota("Miel");
            NotaCata n4 = getOrCreateNota("Panela");
            NotaCata n5 = getOrCreateNota("Avellana");
            NotaCata n6 = getOrCreateNota("Cítricos dulces");
            NotaCata n7 = getOrCreateNota("Caramelo");
            NotaCata n8 = getOrCreateNota("Nuez moscada");
            NotaCata n9 = getOrCreateNota("Cuerpo cremoso");
            NotaCata n10 = getOrCreateNota("Jazmín");
            NotaCata n11 = getOrCreateNota("Durazno blanco");
            NotaCata n12 = getOrCreateNota("Té negro");
            NotaCata n13 = getOrCreateNota("Frutos rojos");
            NotaCata n14 = getOrCreateNota("Flor de jamaica");
            NotaCata n15 = getOrCreateNota("Azúcar rubia");
            NotaCata n16 = getOrCreateNota("Manzana roja");
            NotaCata n17 = getOrCreateNota("Chocolate con leche");
            NotaCata n18 = getOrCreateNota("Vainilla");

            // 1. Cusco Quillabamba
            Product p1 = Product.builder()
                    .slug("cusco")
                    .nombre("Cusco Quillabamba")
                    .imagenUrl("products_images/cafe_quillabamba_bolsa.jpg")
                    .stock(45)
                    .notasCata(List.of(n1, n2, n3))
                    .precios(new ArrayList<>())
                    .build();
            p1.getPrecios().add(Precio.builder().product(p1).peso(250).precio(new BigDecimal("38.00")).build());
            p1.getPrecios().add(Precio.builder().product(p1).peso(500).precio(new BigDecimal("70.00")).build());

            // 2. Villa Rica Alto Palomar
            Product p2 = Product.builder()
                    .slug("villa-rica")
                    .nombre("Villa Rica Alto Palomar")
                    .imagenUrl("products_images/cafe_quillabamba_bolsa.jpg")
                    .stock(30)
                    .notasCata(List.of(n4, n5, n6))
                    .precios(new ArrayList<>())
                    .build();
            p2.getPrecios().add(Precio.builder().product(p2).peso(250).precio(new BigDecimal("42.00")).build());
            p2.getPrecios().add(Precio.builder().product(p2).peso(1000).precio(new BigDecimal("150.00")).build());

            // 3. Chanchamayo Reserve
            Product p3 = Product.builder()
                    .slug("chanchamayo")
                    .nombre("Chanchamayo Reserve")
                    .imagenUrl("products_images/cafe_quillabamba_bolsa.jpg")
                    .stock(60)
                    .notasCata(List.of(n7, n8, n9))
                    .precios(new ArrayList<>())
                    .build();
            p3.getPrecios().add(Precio.builder().product(p3).peso(250).precio(new BigDecimal("35.00")).build());
            p3.getPrecios().add(Precio.builder().product(p3).peso(500).precio(new BigDecimal("65.00")).build());

            // 4. Jaén San Ignacio
            Product p4 = Product.builder()
                    .slug("jaen")
                    .nombre("Jaén San Ignacio")
                    .imagenUrl("products_images/cafe_quillabamba_bolsa.jpg")
                    .stock(25)
                    .notasCata(List.of(n10, n11, n12))
                    .precios(new ArrayList<>())
                    .build();
            p4.getPrecios().add(Precio.builder().product(p4).peso(250).precio(new BigDecimal("45.00")).build());

            // 5. Sandia Puno - Apu Blend
            Product p5 = Product.builder()
                    .slug("sandia-puno")
                    .nombre("Sandia Puno - Apu Blend")
                    .imagenUrl("products_images/cafe_quillabamba_bolsa.jpg")
                    .stock(18)
                    .notasCata(List.of(n13, n14, n15))
                    .precios(new ArrayList<>())
                    .build();
            p5.getPrecios().add(Precio.builder().product(p5).peso(250).precio(new BigDecimal("48.00")).build());
            p5.getPrecios().add(Precio.builder().product(p5).peso(500).precio(new BigDecimal("90.00")).build());

            // 6. Amazonas Rodríguez de Mendoza
            Product p6 = Product.builder()
                    .slug("amazonas")
                    .nombre("Amazonas Rodríguez de Mendoza")
                    .imagenUrl("products_images/cafe_quillabamba_bolsa.jpg")
                    .stock(40)
                    .notasCata(List.of(n16, n17, n18))
                    .precios(new ArrayList<>())
                    .build();
            p6.getPrecios().add(Precio.builder().product(p6).peso(250).precio(new BigDecimal("40.00")).build());
            p6.getPrecios().add(Precio.builder().product(p6).peso(1000).precio(new BigDecimal("145.00")).build());

            productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));
        };
    }

    private NotaCata getOrCreateNota(String nombre) {
        return notaCataRepository.findByNombre(nombre)
                .orElseGet(() -> notaCataRepository.save(NotaCata.builder().nombre(nombre).build()));
    }
}
