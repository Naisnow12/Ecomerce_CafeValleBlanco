package com.valleblanco.backend.controller;

import com.valleblanco.backend.model.Product;
import com.valleblanco.backend.model.Precio;
import com.valleblanco.backend.model.Usuario;
import com.valleblanco.backend.model.Carrito;
import com.valleblanco.backend.model.MovimientoStock;
import com.valleblanco.backend.repository.ProductRepository;
import com.valleblanco.backend.repository.MovimientoStockRepository;
import com.valleblanco.backend.repository.CarritoRepository;
import com.valleblanco.backend.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final CarritoRepository carritoRepository;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        return productRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @AuthenticationPrincipal Usuario admin) {
        if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
            product.setSlug(generarSlug(product.getNombre()));
        }
        if (product.getPrecios() != null) {
            for (Precio p : product.getPrecios()) {
                p.setProduct(product);
            }
        }
        Product saved = productRepository.save(product);
        auditoriaService.registrar(
                admin != null ? admin.getEmail() : "ADMIN",
                "ROLE_ADMIN",
                "CREAR_PRODUCTO",
                "PRODUCTOS",
                saved.getId().toString(),
                "Producto creado: " + saved.getNombre(),
                "EXITO"
        );
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product, @AuthenticationPrincipal Usuario admin) {
        return productRepository.findById(id)
                .map(existing -> {
                    product.setId(id);
                    if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
                        product.setSlug(existing.getSlug() != null ? existing.getSlug() : generarSlug(product.getNombre()));
                    }
                    if (product.getPrecios() != null) {
                        for (Precio p : product.getPrecios()) {
                            p.setProduct(product);
                        }
                    }
                    Product updated = productRepository.save(product);
                    auditoriaService.registrar(
                            admin != null ? admin.getEmail() : "ADMIN",
                            "ROLE_ADMIN",
                            "ACTUALIZAR_PRODUCTO",
                            "PRODUCTOS",
                            updated.getId().toString(),
                            "Producto actualizado: " + updated.getNombre(),
                            "EXITO"
                    );
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal Usuario admin) {
        return productRepository.findById(id).map(prod -> {
            // Desvincular movimientos de inventario asociados
            List<MovimientoStock> movs = movimientoStockRepository.findAll().stream()
                    .filter(m -> m.getProducto() != null && m.getProducto().getId().equals(id))
                    .toList();
            if (!movs.isEmpty()) {
                movimientoStockRepository.deleteAll(movs);
            }

            // Desvincular items de carritos
            List<Carrito> carritos = carritoRepository.findAll();
            for (Carrito c : carritos) {
                boolean mod = c.getItems().removeIf(item -> item.getPrecio() != null &&
                        item.getPrecio().getProduct() != null &&
                        item.getPrecio().getProduct().getId().equals(id));
                if (mod) {
                    carritoRepository.save(c);
                }
            }

            String imagenUrl = prod.getImagenUrl();
            productRepository.delete(prod);
            borrarArchivoImagenSiExiste(imagenUrl);

            auditoriaService.registrar(
                    admin != null ? admin.getEmail() : "ADMIN",
                    "ROLE_ADMIN",
                    "ELIMINAR_PRODUCTO",
                    "PRODUCTOS",
                    id.toString(),
                    "Producto eliminado con ID: " + id + " (" + prod.getNombre() + ")",
                    "EXITO"
            );
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo de imagen no puede estar vacío"));
        }

        try {
            Path targetDir = getImagesDirectory();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String originalName = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalName != null && originalName.lastIndexOf('.') != -1) {
                extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
            }

            String baseName = "imagen";
            if (originalName != null && originalName.lastIndexOf('.') != -1) {
                baseName = originalName.substring(0, originalName.lastIndexOf('.'));
            }
            String safeBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "");
            String filename = "producto-" + System.currentTimeMillis() + "-" + safeBaseName + extension;

            Path targetFile = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "assets/images/" + filename;
            log.info("Imagen subida con éxito: {} guardada en {}", filename, targetFile.toAbsolutePath());

            return ResponseEntity.ok(Map.of("imageUrl", relativePath));
        } catch (IOException e) {
            log.error("Error al guardar la imagen: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al guardar la imagen en el servidor"));
        }
    }

    private Path getImagesDirectory() {
        Path p1 = Paths.get("../Frontend/public/assets/images").toAbsolutePath().normalize();
        if (Files.exists(p1)) return p1;
        Path p2 = Paths.get("Frontend/public/assets/images").toAbsolutePath().normalize();
        if (Files.exists(p2)) return p2;
        try {
            Files.createDirectories(p1);
            return p1;
        } catch (IOException e) {
            return p2;
        }
    }

    private void borrarArchivoImagenSiExiste(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) return;
        try {
            String filename = Paths.get(imagenUrl).getFileName().toString();

            // Proteger imágenes del sistema base
            List<String> protegidas = List.of(
                    "hero.png", "cherries.png", "harvest.png", "drying.png",
                    "gallery-plantation.png", "Valle-Rica-Oxapampa.png", "textura-yute.jpg"
            );
            if (protegidas.contains(filename)) return;

            Path imagePath = getImagesDirectory().resolve(filename);
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
                log.info("Imagen asociada eliminada del disco: {}", imagePath);
            }
        } catch (Exception ex) {
            log.warn("No se pudo eliminar el archivo de imagen {}: {}", imagenUrl, ex.getMessage());
        }
    }

    private String generarSlug(String nombre) {
        if (nombre == null) return "producto-" + System.currentTimeMillis();
        return nombre.toLowerCase()
                .trim()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
