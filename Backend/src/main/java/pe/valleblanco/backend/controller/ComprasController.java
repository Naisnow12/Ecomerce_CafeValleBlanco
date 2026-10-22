package pe.valleblanco.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import pe.valleblanco.backend.dto.Requests;
import pe.valleblanco.backend.service.Autenticacion;
import pe.valleblanco.backend.model.*;
import pe.valleblanco.backend.repo.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ComprasController {
    private final Autenticacion auth;
    private final ProductoRepo productos;
    private final CarritoRepo carrito;
    private final PedidoRepo pedidos;

    public ComprasController(Autenticacion a, ProductoRepo p, CarritoRepo c, PedidoRepo o) {
        auth = a;
        productos = p;
        carrito = c;
        pedidos = o;
    }

    @GetMapping("/carrito")
    public List<CarritoItem> ver(@RequestHeader(value = "Authorization", required = false) String h) {
        return carrito.findByUsuarioIdOrderById(auth.usuario(h).id);
    }

    @PostMapping("/carrito")
    @Transactional
    public CarritoItem agregar(@RequestHeader(value = "Authorization", required = false) String h,
            @Valid @RequestBody Requests.CarritoInput r) {
        Usuario u = auth.usuario(h);
        Producto p = productos.findById(r.productoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (!p.estado)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inactivo");
        CarritoItem item = carrito.findByUsuarioIdAndProductoId(u.id, p.id).orElseGet(() -> {
            CarritoItem nuevo = new CarritoItem();
            nuevo.usuario = u;
            nuevo.producto = p;
            return nuevo;
        });
        long total = (long) item.cantidad + r.cantidad();
        if (total > p.stock)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
        item.cantidad = (int) total;
        return carrito.save(item);
    }

    @PutMapping("/carrito/{productoId}")
    @Transactional
    public CarritoItem cantidad(@RequestHeader(value = "Authorization", required = false) String h,
            @PathVariable Long productoId, @Valid @RequestBody Requests.CantidadInput r) {
        Usuario u = auth.usuario(h);
        CarritoItem item = carrito.findByUsuarioIdAndProductoId(u.id, productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No está en el carrito"));
        if (!item.producto.estado || r.cantidad() > item.producto.stock)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente o producto inactivo");
        item.cantidad = r.cantidad();
        return carrito.save(item);
    }

    @DeleteMapping("/carrito/{productoId}")
    @Transactional
    public void eliminar(@RequestHeader(value = "Authorization", required = false) String h,
            @PathVariable Long productoId) {
        Usuario u = auth.usuario(h);
        CarritoItem item = carrito.findByUsuarioIdAndProductoId(u.id, productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No está en el carrito"));
        carrito.delete(item);
    }

    @PostMapping("/pedidos")
    @Transactional
    public Pedido comprar(@RequestHeader(value = "Authorization", required = false) String h,
            @Valid @RequestBody Requests.PedidoInput r) {
        Usuario u = auth.usuario(h);
        String metodo = r.metodoPago().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("YAPE", "PLIN", "TRANSFERENCIA").contains(metodo))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Método de pago inválido");
        List<CarritoItem> items = carrito.findByUsuarioIdOrderById(u.id);
        if (items.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrito vacío");
        Pedido pedido = new Pedido();
        pedido.usuario = u;
        pedido.metodoPago = metodo;
        pedido.direccionEnvio = r.direccionEnvio().trim();
        pedido.total = BigDecimal.ZERO;
        for (CarritoItem item : items) {
            Producto p = productos.bloquear(item.producto.id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente"));
            if (!p.estado || item.cantidad < 1 || p.stock < item.cantidad)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto sin stock suficiente: " + p.nombre);
            PedidoDetalle d = new PedidoDetalle();
            d.pedido = pedido;
            d.producto = p;
            d.cantidad = item.cantidad;
            d.precioUnitario = p.precio;
            d.subtotal = p.precio.multiply(BigDecimal.valueOf(item.cantidad));
            pedido.detalles.add(d);
            pedido.total = pedido.total.add(d.subtotal);
            p.stock -= item.cantidad;
        }
        pedidos.saveAndFlush(pedido);
        carrito.deleteByUsuarioId(u.id);
        return pedido;
    }

    @GetMapping("/pedidos")
    public List<Pedido> listar(@RequestHeader(value = "Authorization", required = false) String h) {
        return pedidos.findByUsuarioIdOrderByCreadoEnDesc(auth.usuario(h).id);
    }
}
