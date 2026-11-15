package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.MovimientoStockRequest;
import com.valleblanco.backend.model.MovimientoStock;
import com.valleblanco.backend.model.Product;
import com.valleblanco.backend.repository.MovimientoStockRepository;
import com.valleblanco.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductRepository productRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public MovimientoStock registrarMovimiento(MovimientoStockRequest request, String emailUsuario, String rolUsuario) {
        Product producto = productRepository.findById(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + request.getProductoId()));

        int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
        int cantidad = request.getCantidad() != null ? request.getCantidad() : 0;
        int nuevoStock = stockAnterior;

        String tipo = request.getTipo().toUpperCase();
        if ("ENTRADA".equals(tipo)) {
            nuevoStock = stockAnterior + cantidad;
        } else if ("SALIDA".equals(tipo)) {
            if (stockAnterior < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para realizar salida. Stock actual: " + stockAnterior);
            }
            nuevoStock = stockAnterior - cantidad;
        } else if ("AJUSTE".equals(tipo)) {
            nuevoStock = cantidad; // Para ajuste, cantidad representa el stock real contado
        } else {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipo);
        }

        producto.setStock(nuevoStock);
        productRepository.save(producto);

        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .tipo(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(nuevoStock)
                .motivo(request.getMotivo())
                .emailUsuario(emailUsuario)
                .creadoEn(LocalDateTime.now())
                .build();

        MovimientoStock guardado = movimientoStockRepository.save(movimiento);

        auditoriaService.registrar(
                emailUsuario,
                rolUsuario,
                "MOVIMIENTO_STOCK_" + tipo,
                "INVENTARIO",
                producto.getId().toString(),
                "Stock anterior: " + stockAnterior + ", nuevo: " + nuevoStock + ", motivo: " + request.getMotivo(),
                "EXITO"
        );

        return guardado;
    }

    @Transactional
    public Product actualizarNivelMinimo(Long productoId, Integer nivelMinimo, String emailUsuario, String rolUsuario) {
        Product producto = productRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        producto.setNivelMinimoStock(nivelMinimo);
        Product saved = productRepository.save(producto);

        auditoriaService.registrar(
                emailUsuario,
                rolUsuario,
                "ACTUALIZAR_STOCK_MINIMO",
                "INVENTARIO",
                productoId.toString(),
                "Nuevo nivel mínimo: " + nivelMinimo,
                "EXITO"
        );

        return saved;
    }

    public List<MovimientoStock> obtenerHistorial() {
        return movimientoStockRepository.findAllByOrderByCreadoEnDesc();
    }

    public List<Product> obtenerAlertasStockBajo() {
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= (p.getNivelMinimoStock() != null ? p.getNivelMinimoStock() : 10))
                .toList();
    }

    public Map<String, Object> obtenerDashboardInventario() {
        List<Product> productos = productRepository.findAll();
        List<MovimientoStock> movimientos = movimientoStockRepository.findAll();

        long totalProductos = productos.size();
        long totalExistencias = productos.stream().mapToLong(p -> p.getStock() != null ? p.getStock() : 0).sum();
        long stockBajo = productos.stream().filter(p -> p.getStock() != null && p.getStock() > 0 && p.getStock() <= (p.getNivelMinimoStock() != null ? p.getNivelMinimoStock() : 10)).count();
        long agotados = productos.stream().filter(p -> p.getStock() == null || p.getStock() <= 0).count();

        long totalEntradas = movimientos.stream().filter(m -> "ENTRADA".equalsIgnoreCase(m.getTipo())).mapToLong(MovimientoStock::getCantidad).sum();
        long totalSalidas = movimientos.stream().filter(m -> "SALIDA".equalsIgnoreCase(m.getTipo())).mapToLong(MovimientoStock::getCantidad).sum();

        return Map.of(
                "totalProductos", totalProductos,
                "totalExistencias", totalExistencias,
                "stockBajo", stockBajo,
                "agotados", agotados,
                "totalEntradas", totalEntradas,
                "totalSalidas", totalSalidas,
                "productos", productos,
                "ultimosMovimientos", movimientoStockRepository.findTop20ByOrderByCreadoEnDesc(),
                "alertasStock", obtenerAlertasStockBajo()
        );
    }
}
