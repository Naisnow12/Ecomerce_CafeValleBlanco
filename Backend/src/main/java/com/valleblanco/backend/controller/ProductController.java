package com.valleblanco.backend.controller;

import com.valleblanco.backend.model.Product;
import com.valleblanco.backend.model.Usuario;
import com.valleblanco.backend.repository.ProductRepository;
import com.valleblanco.backend.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductRepository productRepository;
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
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        auditoriaService.registrar(
                admin != null ? admin.getEmail() : "ADMIN",
                "ROLE_ADMIN",
                "ELIMINAR_PRODUCTO",
                "PRODUCTOS",
                id.toString(),
                "Producto eliminado con ID: " + id,
                "EXITO"
        );
        return ResponseEntity.ok().build();
    }
}
