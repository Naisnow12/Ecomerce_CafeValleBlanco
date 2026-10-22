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
            // Re-poblamos con las 10 presentaciones oficiales de Oxapampa
            productRepository.deleteAll();

            NotaCata nChocolate = getOrCreateNota("Chocolate");
            NotaCata nDurazno = getOrCreateNota("Durazno");
            NotaCata nCaramelo = getOrCreateNota("Caramelo");
            NotaCata nFloral = getOrCreateNota("Floral");
            NotaCata nCitrico = getOrCreateNota("Cítrico");
            NotaCata nMiel = getOrCreateNota("Miel");
            NotaCata nCacaoOscuro = getOrCreateNota("Cacao oscuro");
            NotaCata nNuezTostada = getOrCreateNota("Nuez tostada");
            NotaCata nPanela = getOrCreateNota("Panela");
            NotaCata nFrutosRojos = getOrCreateNota("Frutos rojos");
            NotaCata nJazmin = getOrCreateNota("Jazmín");
            NotaCata nVainilla = getOrCreateNota("Vainilla");
            NotaCata nAlmendra = getOrCreateNota("Almendra");
            NotaCata nCanela = getOrCreateNota("Canela");

            List<Product> productos = new ArrayList<>();

            // 1. Valle Blanco Clásico (Molido medio) - Imagen Verde
            Product p1 = Product.builder()
                    .slug("valle-blanco-clasico")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Clásico")
                    .tipoCafe("Café Molido")
                    .molienda("Molido medio")
                    .tueste("Medio")
                    .variedad("Caturra / Typica")
                    .altitud("1,600 msnm")
                    .descripcion("Molido artesanalmente para filtrado, prensa francesa y métodos de goteo. Listo para preparar en casa con la frescura y el aroma del grano recién tostado en Villa Rica.")
                    .imagenUrl("assets/images/producto-molido.jpg")
                    .stock(50)
                    .badgeText("MÁS POPULAR")
                    .esDestacado(true)
                    .esOscuro(false)
                    .notasCata(List.of(nChocolate, nDurazno, nCaramelo))
                    .precios(new ArrayList<>())
                    .build();
            p1.getPrecios().add(Precio.builder().product(p1).peso(250).precio(new BigDecimal("35.00")).build());
            p1.getPrecios().add(Precio.builder().product(p1).peso(500).precio(new BigDecimal("65.00")).build());
            productos.add(p1);

            // 2. Valle Blanco Origen (En Grano) - Imagen Kraft
            Product p2 = Product.builder()
                    .slug("valle-blanco-origen")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Origen")
                    .tipoCafe("Café en Grano")
                    .molienda("En Grano")
                    .tueste("Medio claro")
                    .variedad("Typica")
                    .altitud("1,800 msnm")
                    .descripcion("Para los que prefieren controlar cada detalle. Granos enteros tostados artesanalmente, listos para moler al momento y aprovechar al máximo el perfil aromático de Oxapampa.")
                    .imagenUrl("assets/images/producto-grano.jpg")
                    .stock(40)
                    .esDestacado(true)
                    .esOscuro(false)
                    .notasCata(List.of(nFloral, nCitrico, nMiel))
                    .precios(new ArrayList<>())
                    .build();
            p2.getPrecios().add(Precio.builder().product(p2).peso(340).precio(new BigDecimal("42.00")).build());
            p2.getPrecios().add(Precio.builder().product(p2).peso(1000).precio(new BigDecimal("120.00")).build());
            productos.add(p2);

            // 3. Valle Blanco Espresso (Espresso) - Imagen Negra
            Product p3 = Product.builder()
                    .slug("valle-blanco-espresso")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Espresso")
                    .tipoCafe("Espresso")
                    .molienda("Espresso")
                    .tueste("Oscuro")
                    .variedad("Caturra")
                    .altitud("1,400 msnm")
                    .descripcion("Tueste oscuro de precisión para extracciones intensas. Cuerpo pleno, crema dorada y un aroma profundo que permanece en cada taza de espresso o americano.")
                    .imagenUrl("assets/images/producto-espresso.jpg")
                    .stock(35)
                    .badgeText("INTENSO")
                    .esDestacado(true)
                    .esOscuro(true)
                    .notasCata(List.of(nCacaoOscuro, nNuezTostada, nPanela))
                    .precios(new ArrayList<>())
                    .build();
            p3.getPrecios().add(Precio.builder().product(p3).peso(250).precio(new BigDecimal("38.00")).build());
            p3.getPrecios().add(Precio.builder().product(p3).peso(500).precio(new BigDecimal("70.00")).build());
            productos.add(p3);

            // 4. Valle Blanco Reserva Oxapampa (Molido fino) - Imagen Kraft
            Product p4 = Product.builder()
                    .slug("valle-blanco-reserva-oxapampa")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Reserva Oxapampa")
                    .tipoCafe("Café Molido")
                    .molienda("Molido fino")
                    .tueste("Medio")
                    .variedad("Bourbon / Typica")
                    .altitud("1,750 msnm")
                    .descripcion("Molienda calibrada para cafetera italiana Moka y Aeropress. Perfil dulce con notas acarameladas y acidez suave cultivado en las laderas de Villa Rica.")
                    .imagenUrl("assets/images/producto-grano.jpg")
                    .stock(28)
                    .badgeText("RESERVA")
                    .esDestacado(false)
                    .esOscuro(false)
                    .notasCata(List.of(nCaramelo, nPanela, nAlmendra))
                    .precios(new ArrayList<>())
                    .build();
            p4.getPrecios().add(Precio.builder().product(p4).peso(250).precio(new BigDecimal("38.00")).build());
            productos.add(p4);

            // 5. Valle Blanco Altura Geisha (En Grano) - Imagen Verde
            Product p5 = Product.builder()
                    .slug("valle-blanco-altura-geisha")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Altura Geisha")
                    .tipoCafe("Especialidad")
                    .molienda("En Grano")
                    .tueste("Claro")
                    .variedad("Geisha")
                    .altitud("1,850 msnm")
                    .descripcion("Micro-lote exclusivo de gran altitud en Oxapampa. Notas florales a jazmín y frutas tropicales con cuerpo sedoso y elegante.")
                    .imagenUrl("assets/images/producto-molido.jpg")
                    .stock(15)
                    .badgeText("EDICIÓN LIMITADA")
                    .esDestacado(true)
                    .esOscuro(false)
                    .notasCata(List.of(nJazmin, nCitrico, nDurazno))
                    .precios(new ArrayList<>())
                    .build();
            p5.getPrecios().add(Precio.builder().product(p5).peso(250).precio(new BigDecimal("55.00")).build());
            productos.add(p5);

            // 6. Valle Blanco Prensa Francesa (Molido grueso) - Imagen Verde
            Product p6 = Product.builder()
                    .slug("valle-blanco-prensa-francesa")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Prensa & Cold Brew")
                    .tipoCafe("Café Molido")
                    .molienda("Molido grueso")
                    .tueste("Medio")
                    .variedad("Caturra")
                    .altitud("1,550 msnm")
                    .descripcion("Molienda gruesa uniforme ideal para inmersión prolongada, prensa francesa o cold brew artesanal. Extrae dulzura sin amargor.")
                    .imagenUrl("assets/images/producto-molido.jpg")
                    .stock(30)
                    .esDestacado(false)
                    .esOscuro(false)
                    .notasCata(List.of(nChocolate, nVainilla, nNuezTostada))
                    .precios(new ArrayList<>())
                    .build();
            p6.getPrecios().add(Precio.builder().product(p6).peso(250).precio(new BigDecimal("36.00")).build());
            p6.getPrecios().add(Precio.builder().product(p6).peso(500).precio(new BigDecimal("68.00")).build());
            productos.add(p6);

            // 7. Valle Blanco Tostado Intenso (Espresso) - Imagen Negra
            Product p7 = Product.builder()
                    .slug("valle-blanco-tostado-intenso")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Intenso Dark")
                    .tipoCafe("Espresso")
                    .molienda("Espresso")
                    .tueste("Oscuro")
                    .variedad("Pache / Caturra")
                    .altitud("1,450 msnm")
                    .descripcion("Café para paladares que adoran notas de chocolate amargo pronunciadas y un postgusto prolongado, perfecto con leche o capuchino.")
                    .imagenUrl("assets/images/producto-espresso.jpg")
                    .stock(25)
                    .esDestacado(false)
                    .esOscuro(true)
                    .notasCata(List.of(nCacaoOscuro, nChocolate, nCanela))
                    .precios(new ArrayList<>())
                    .build();
            p7.getPrecios().add(Precio.builder().product(p7).peso(250).precio(new BigDecimal("36.00")).build());
            p7.getPrecios().add(Precio.builder().product(p7).peso(1000).precio(new BigDecimal("110.00")).build());
            productos.add(p7);

            // 8. Valle Blanco Miel Honey (En Grano) - Imagen Kraft
            Product p8 = Product.builder()
                    .slug("valle-blanco-miel-honey")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Honey Process")
                    .tipoCafe("Especialidad")
                    .molienda("En Grano")
                    .tueste("Medio claro")
                    .variedad("Catimor / Typica")
                    .altitud("1,680 msnm")
                    .descripcion("Proceso Yellow Honey secado al sol en Oxapampa conservando el mucílago dulce. Desarrolla una taza acaramelada y frutos rojos naturales.")
                    .imagenUrl("assets/images/producto-grano.jpg")
                    .stock(20)
                    .badgeText("HONEY PROCESS")
                    .esDestacado(false)
                    .esOscuro(false)
                    .notasCata(List.of(nMiel, nFrutosRojos, nPanela))
                    .precios(new ArrayList<>())
                    .build();
            p8.getPrecios().add(Precio.builder().product(p8).peso(340).precio(new BigDecimal("46.00")).build());
            productos.add(p8);

            // 9. Valle Blanco Filtro V60 (Molido fino) - Imagen Verde
            Product p9 = Product.builder()
                    .slug("valle-blanco-filtro-v60")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Filtro Pour-Over")
                    .tipoCafe("Café Molido")
                    .molienda("Molido fino")
                    .tueste("Medio claro")
                    .variedad("Typica")
                    .altitud("1,700 msnm")
                    .descripcion("Calibración exacta para conos V60 y Chemex. Proporciona una taza limpia y brillante donde destacan sutiles notas a jazmín y durazno.")
                    .imagenUrl("assets/images/producto-molido.jpg")
                    .stock(32)
                    .esDestacado(false)
                    .esOscuro(false)
                    .notasCata(List.of(nDurazno, nCitrico, nJazmin))
                    .precios(new ArrayList<>())
                    .build();
            p9.getPrecios().add(Precio.builder().product(p9).peso(250).precio(new BigDecimal("38.00")).build());
            productos.add(p9);

            // 10. Valle Blanco Selección Familiar (En Grano) - Imagen Kraft
            Product p10 = Product.builder()
                    .slug("valle-blanco-seleccion-familiar")
                    .nombre("Valle Blanco")
                    .nombreSecundario("Selección Familiar")
                    .tipoCafe("Café en Grano")
                    .molienda("En Grano")
                    .tueste("Medio")
                    .variedad("Caturra / Typica")
                    .altitud("1,650 msnm")
                    .descripcion("El café insignia de la familia Valle Blanco en Villa Rica. Un balance redondo entre dulzor achocolatado, notas a nuez y acidez cítrica moderada.")
                    .imagenUrl("assets/images/producto-grano.jpg")
                    .stock(45)
                    .badgeText("INSIGNIA")
                    .esDestacado(true)
                    .esOscuro(false)
                    .notasCata(List.of(nChocolate, nNuezTostada, nMiel))
                    .precios(new ArrayList<>())
                    .build();
            p10.getPrecios().add(Precio.builder().product(p10).peso(340).precio(new BigDecimal("40.00")).build());
            p10.getPrecios().add(Precio.builder().product(p10).peso(1000).precio(new BigDecimal("115.00")).build());
            productos.add(p10);

            productRepository.saveAll(productos);
        };
    }

    private NotaCata getOrCreateNota(String nombre) {
        return notaCataRepository.findByNombre(nombre)
                .orElseGet(() -> notaCataRepository.save(NotaCata.builder().nombre(nombre).build()));
    }
}
