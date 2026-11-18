package com.valleblanco.backend.config;

import com.valleblanco.backend.model.*;
import com.valleblanco.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ProductRepository productRepository;
    private final NotaCataRepository notaCataRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final PedidoRepository pedidoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final CarritoRepository carritoRepository;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // 1. Inicializar roles si no existen
            Role rolAdmin = getOrCreateRol("ROLE_ADMIN");
            Role rolStock = getOrCreateRol("ROLE_ENCARGADO_STOCK");
            Role rolCliente = getOrCreateRol("ROLE_CLIENTE");

            // 2. Inicializar usuarios por defecto si no existen
            crearUsuarioSiNoExiste(
                    "admin@valleblanco.pe",
                    "Administrador",
                    "Sistema",
                    "admin123",
                    "999888777",
                    List.of(rolAdmin)
            );

            crearUsuarioSiNoExiste(
                    "stock@valleblanco.pe",
                    "Encargado",
                    "Almacén",
                    "stock123",
                    "999888776",
                    List.of(rolStock)
            );

            crearUsuarioSiNoExiste(
                    "cliente@valleblanco.pe",
                    "Carlos",
                    "Mendoza",
                    "cliente123",
                    "999888775",
                    List.of(rolCliente)
            );

            // 3. Sincronizar movimientos pasados si hubo pedidos creados antes de activar el registro de movimientos
            try {
                if (movimientoStockRepository.count() == 0 && pedidoRepository.count() > 0) {
                    log.info("Sincronizando movimientos de inventario históricos para pedidos previos...");
                    List<Pedido> pedidosPrevios = pedidoRepository.findAll();
                    for (Pedido p : pedidosPrevios) {
                        for (PedidoItem item : p.getItems()) {
                            Product prod = productRepository.findById(item.getProductoId()).orElse(null);
                            if (prod != null) {
                                int stockActual = prod.getStock() != null ? prod.getStock() : 0;
                                MovimientoStock mov = MovimientoStock.builder()
                                        .producto(prod)
                                        .tipo("SALIDA")
                                        .cantidad(item.getCantidad())
                                        .stockAnterior(stockActual + item.getCantidad())
                                        .stockNuevo(stockActual)
                                        .motivo("Venta en pedido #" + p.getNumeroPedido())
                                        .emailUsuario(p.getUsuario() != null ? p.getUsuario().getEmail() : "cliente@valleblanco.pe")
                                        .creadoEn(p.getCreadoEn())
                                        .build();
                                movimientoStockRepository.save(mov);
                            }
                        }
                    }
                    log.info("Sincronización de pedidos históricos a inventario completada.");
                }
            } catch (Exception e) {
                log.warn("No se pudo sincronizar pedidos previos: {}", e.getMessage());
            }

            // 4. Inicializar / Sincronizar las 9 Presentaciones Oficiales de Valle Blanco
            inicializarPresentacionesValleBlanco();

            // 5. Eliminar productos obsoletos de prueba (debe haber estrictamente las 9 presentaciones oficiales)
            limpiarProductosObsoletos();
        };
    }

    private void inicializarPresentacionesValleBlanco() {
        // Notas de cata
        NotaCata nChocolate = getOrCreateNota("Chocolate");
        NotaCata nDurazno = getOrCreateNota("Durazno");
        NotaCata nCaramelo = getOrCreateNota("Caramelo");
        NotaCata nFloral = getOrCreateNota("Floral");
        NotaCata nCitrico = getOrCreateNota("Cítrico");
        NotaCata nMiel = getOrCreateNota("Miel");
        NotaCata nCacaoOscuro = getOrCreateNota("Cacao oscuro");
        NotaCata nNuezTostada = getOrCreateNota("Nuez tostada");
        NotaCata nPanela = getOrCreateNota("Panela");
        NotaCata nJazmin = getOrCreateNota("Jazmín");
        NotaCata nMandarina = getOrCreateNota("Mandarina");
        NotaCata nFrutosAmarillos = getOrCreateNota("Frutos amarillos");
        NotaCata nAlmendra = getOrCreateNota("Almendra");
        NotaCata nFresa = getOrCreateNota("Fresa");
        NotaCata nCereza = getOrCreateNota("Cereza");
        NotaCata nNaranja = getOrCreateNota("Naranja");
        NotaCata nAvellana = getOrCreateNota("Avellana");
        NotaCata nAzucarRubia = getOrCreateNota("Azúcar rubia");
        NotaCata nCacao = getOrCreateNota("Cacao");
        NotaCata nCiruela = getOrCreateNota("Ciruela");
        NotaCata nVainilla = getOrCreateNota("Vainilla");

        // 1. Valle Blanco Clásico
        upsertProduct(
                "valle-blanco-clasico",
                "Valle Blanco",
                "Clásico",
                "Café Molido",
                "Molido medio",
                "Medio",
                "Caturra / Typica",
                "1,600 msnm",
                "Molido artesanalmente para filtrado, prensa francesa y métodos de goteo. Listo para preparar en casa con la frescura y el aroma del grano recién tostado en Villa Rica.",
                "assets/images/producto-molido.jpg",
                50,
                10,
                "MÁS POPULAR",
                true,
                false,
                List.of(nChocolate, nDurazno, nCaramelo),
                List.of(
                        new PesoPrecio(250, new BigDecimal("35.00")),
                        new PesoPrecio(500, new BigDecimal("65.00"))
                )
        );

        // 2. Valle Blanco Origen
        upsertProduct(
                "valle-blanco-origen",
                "Valle Blanco",
                "Origen",
                "Café en Grano",
                "En Grano",
                "Medio claro",
                "Typica",
                "1,800 msnm",
                "Para los que prefieren controlar cada detalle. Granos enteros tostados artesanalmente, listos para moler al momento y aprovechar al máximo el perfil aromático de Oxapampa.",
                "assets/images/producto-grano.jpg",
                40,
                10,
                "ORIGEN PURO",
                true,
                false,
                List.of(nFloral, nCitrico, nMiel),
                List.of(
                        new PesoPrecio(340, new BigDecimal("42.00")),
                        new PesoPrecio(1000, new BigDecimal("120.00"))
                )
        );

        // 3. Valle Blanco Espresso
        upsertProduct(
                "valle-blanco-espresso",
                "Valle Blanco",
                "Espresso",
                "Espresso",
                "Espresso",
                "Oscuro",
                "Caturra",
                "1,400 msnm",
                "Tueste oscuro de precisión para extracciones intensas. Cuerpo pleno, crema dorada y un aroma profundo que permanece en cada taza de espresso o americano.",
                "assets/images/producto-espresso.jpg",
                35,
                10,
                "INTENSO",
                true,
                true,
                List.of(nCacaoOscuro, nNuezTostada, nPanela),
                List.of(
                        new PesoPrecio(250, new BigDecimal("38.00")),
                        new PesoPrecio(500, new BigDecimal("70.00"))
                )
        );

        // 4. Valle Blanco Amanecer
        upsertProduct(
                "valle-blanco-amanecer",
                "Valle Blanco",
                "Amanecer",
                "Café en Grano",
                "En Grano",
                "Claro",
                "Typica",
                "1,800 msnm",
                "Un café delicado y aromático, elaborado para quienes disfrutan de sabores suaves y complejos. Su tueste claro permite apreciar las características naturales del grano y sus notas florales y frutales.",
                "assets/images/producto-Amanecer.png",
                35,
                10,
                "PERFIL FLORAL",
                true,
                false,
                List.of(nJazmin, nMandarina, nDurazno),
                List.of(new PesoPrecio(250, new BigDecimal("44.00")))
        );

        // 5. Valle Blanco Miel de Montaña
        upsertProduct(
                "valle-blanco-miel-montana",
                "Valle Blanco",
                "Miel de Montaña",
                "Especialidad",
                "En Grano",
                "Medio",
                "Caturra",
                "1,650 msnm",
                "Un café especial procesado mediante el método honey, que conserva parte del mucílago durante el secado. Ofrece una taza dulce, equilibrada y con una textura agradable.",
                "assets/images/producto-Mieldemontaña.png",
                28,
                10,
                "PROCESO HONEY",
                true,
                false,
                List.of(nMiel, nFrutosAmarillos, nAlmendra),
                List.of(new PesoPrecio(340, new BigDecimal("46.00")))
        );

        // 6. Valle Blanco Frutos Rojos
        upsertProduct(
                "valle-blanco-frutos-rojos",
                "Valle Blanco",
                "Frutos Rojos",
                "Especialidad",
                "En Grano",
                "Medio",
                "Caturra / Typica",
                "1,600 msnm",
                "Un café de carácter expresivo, cuyos granos se secan junto con la cereza del café. Su perfil destaca por sus notas frutales, dulces y una experiencia aromática intensa.",
                "assets/images/producto-Frutosrojos.png",
                22,
                10,
                "PROCESO NATURAL",
                false,
                true,
                List.of(nFresa, nCereza, nChocolate),
                List.of(new PesoPrecio(250, new BigDecimal("42.00")))
        );

        // 7. Valle Blanco Reserva de Altura
        upsertProduct(
                "valle-blanco-reserva-altura",
                "Valle Blanco",
                "Reserva de Altura",
                "Especialidad",
                "En Grano",
                "Medio claro",
                "Typica / Bourbon",
                "1,900 msnm",
                "Una selección especial de granos cultivados en zonas de mayor altitud de Villa Rica. Su perfil combina delicadeza aromática, dulzor natural y una acidez equilibrada.",
                "assets/images/producto-Reservadealtura.png",
                15,
                10,
                "EDICIÓN ESPECIAL",
                true,
                true,
                List.of(nNaranja, nCaramelo, nJazmin),
                List.of(new PesoPrecio(340, new BigDecimal("58.00")))
        );

        // 8. Valle Blanco Tradición
        upsertProduct(
                "valle-blanco-tradicion",
                "Valle Blanco",
                "Tradición",
                "Café Molido",
                "Molido medio",
                "Medio oscuro",
                "Caturra",
                "1,500 msnm",
                "Inspirado en el sabor del café peruano de toda la vida. Su cuerpo equilibrado y su dulzor tostado lo convierten en una excelente opción para disfrutar diariamente en casa.",
                "assets/images/producto-Tradicion.png",
                50,
                10,
                "TRADICIONAL",
                false,
                false,
                List.of(nChocolate, nAvellana, nAzucarRubia),
                List.of(new PesoPrecio(250, new BigDecimal("33.00")))
        );

        // 9. Valle Blanco Cold Brew
        upsertProduct(
                "valle-blanco-cold-brew",
                "Valle Blanco",
                "Cold Brew",
                "Café Molido",
                "Molido grueso",
                "Medio",
                "Caturra / Typica",
                "1,600 msnm",
                "Una propuesta pensada para los amantes del café frío. Su molienda gruesa facilita la preparación mediante infusión prolongada en agua fría, obteniendo una bebida refrescante y de perfil dulce.",
                "assets/images/producto-ColdBrew.png",
                32,
                10,
                "EXTRACCIÓN FRÍA",
                false,
                false,
                List.of(nCacao, nCiruela, nVainilla),
                List.of(new PesoPrecio(250, new BigDecimal("38.00")))
        );

        log.info("Sincronización de las 9 presentaciones de Café Valle Blanco finalizada.");
    }

    private void limpiarProductosObsoletos() {
        List<String> slugsOficiales = List.of(
                "valle-blanco-clasico",
                "valle-blanco-origen",
                "valle-blanco-espresso",
                "valle-blanco-amanecer",
                "valle-blanco-miel-montana",
                "valle-blanco-frutos-rojos",
                "valle-blanco-reserva-altura",
                "valle-blanco-tradicion",
                "valle-blanco-cold-brew"
        );

        List<Product> todos = productRepository.findAll();
        for (Product prod : todos) {
            if (!slugsOficiales.contains(prod.getSlug())) {
                log.info("Eliminando producto antiguo obsoleto de la BD: id={}, nombre={}, slug={}", prod.getId(), prod.getNombre(), prod.getSlug());
                try {
                    // Desvincular movimientos de stock asociados
                    List<MovimientoStock> movs = movimientoStockRepository.findAll().stream()
                            .filter(m -> m.getProducto() != null && m.getProducto().getId().equals(prod.getId()))
                            .toList();
                    if (!movs.isEmpty()) {
                        movimientoStockRepository.deleteAll(movs);
                    }

                    // Desvincular items de carrito asociados
                    List<Carrito> carritos = carritoRepository.findAll();
                    for (Carrito c : carritos) {
                        boolean modificado = c.getItems().removeIf(item -> item.getPrecio() != null &&
                                item.getPrecio().getProduct() != null &&
                                item.getPrecio().getProduct().getId().equals(prod.getId()));
                        if (modificado) {
                            carritoRepository.save(c);
                        }
                    }

                    productRepository.delete(prod);
                    log.info("Producto obsoleto eliminado exitosamente: {}", prod.getSlug());
                } catch (Exception ex) {
                    log.warn("No se pudo eliminar el producto obsoleto {}: {}", prod.getSlug(), ex.getMessage());
                }
            }
        }
    }

    private record PesoPrecio(int peso, BigDecimal precio) {}

    private void upsertProduct(
            String slug,
            String nombre,
            String nombreSecundario,
            String tipoCafe,
            String molienda,
            String tueste,
            String variedad,
            String altitud,
            String descripcion,
            String imagenUrl,
            int stock,
            int nivelMinimo,
            String badgeText,
            boolean esDestacado,
            boolean esOscuro,
            List<NotaCata> notas,
            List<PesoPrecio> preciosList
    ) {
        Optional<Product> opt = productRepository.findBySlug(slug);
        Product p = opt.orElseGet(() -> Product.builder().slug(slug).precios(new ArrayList<>()).build());

        p.setNombre(nombre);
        p.setNombreSecundario(nombreSecundario);
        p.setTipoCafe(tipoCafe);
        p.setMolienda(molienda);
        p.setTueste(tueste);
        p.setVariedad(variedad);
        p.setAltitud(altitud);
        p.setDescripcion(descripcion);
        p.setImagenUrl(imagenUrl);
        if (opt.isEmpty() || p.getStock() == null) {
            p.setStock(stock);
        }
        p.setNivelMinimoStock(nivelMinimo);
        p.setBadgeText(badgeText);
        p.setEsDestacado(esDestacado);
        p.setEsOscuro(esOscuro);
        p.setNotasCata(new ArrayList<>(notas));

        if (p.getPrecios() == null) {
            p.setPrecios(new ArrayList<>());
        }

        if (p.getPrecios().isEmpty()) {
            for (PesoPrecio pp : preciosList) {
                p.getPrecios().add(Precio.builder().product(p).peso(pp.peso()).precio(pp.precio()).build());
            }
        }

        productRepository.save(p);
    }

    private Role getOrCreateRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Role.builder().nombre(nombre).build()));
    }

    private void crearUsuarioSiNoExiste(String email, String nombres, String apellidos, String rawPassword, String telefono, List<Role> roles) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario u = Usuario.builder()
                    .email(email)
                    .nombres(nombres)
                    .apellidos(apellidos)
                    .telefono(telefono)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .activo(true)
                    .creadoEn(LocalDateTime.now())
                    .actualizadoEn(LocalDateTime.now())
                    .roles(new ArrayList<>(roles))
                    .build();
            usuarioRepository.save(u);
        }
    }

    private NotaCata getOrCreateNota(String nombre) {
        return notaCataRepository.findByNombre(nombre)
                .orElseGet(() -> notaCataRepository.save(NotaCata.builder().nombre(nombre).build()));
    }
}
