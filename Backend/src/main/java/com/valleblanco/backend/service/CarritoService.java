package com.valleblanco.backend.service;

import com.valleblanco.backend.dto.ActualizarCantidadRequest;
import com.valleblanco.backend.dto.AgregarAlCarritoRequest;
import com.valleblanco.backend.dto.CarritoItemResponse;
import com.valleblanco.backend.dto.CarritoResponse;
import com.valleblanco.backend.model.*;
import com.valleblanco.backend.repository.CarritoRepository;
import com.valleblanco.backend.repository.PrecioRepository;
import com.valleblanco.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrecioRepository precioRepository;

    private Usuario findUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
    }

    private Carrito getOrCreateCarrito(Usuario usuario) {
        return carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevo = Carrito.builder()
                            .usuario(usuario)
                            .actualizadoEn(LocalDateTime.now())
                            .build();
                    return carritoRepository.save(nuevo);
                });
    }

    public CarritoResponse getCarrito(String email) {
        Usuario usuario = findUsuario(email);
        Carrito carrito = getOrCreateCarrito(usuario);
        return toCarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse agregarItem(String email, AgregarAlCarritoRequest request) {
        Usuario usuario = findUsuario(email);
        Carrito carrito = getOrCreateCarrito(usuario);

        Precio precio = precioRepository.findById(request.getPrecioId())
                .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + request.getPrecioId()));

        // Verificar stock
        Product producto = precio.getProduct();
        int cantidadSolicitada = request.getCantidad();

        // Verificar si ya existe el item en el carrito
        Optional<CarritoItem> itemExistente = carrito.getItems().stream()
                .filter(i -> i.getPrecio().getId().equals(precio.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            CarritoItem item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidadSolicitada;
            if (nuevaCantidad > producto.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStock());
            }
            item.setCantidad(nuevaCantidad);
        } else {
            if (cantidadSolicitada > producto.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStock());
            }
            CarritoItem nuevoItem = CarritoItem.builder()
                    .carrito(carrito)
                    .precio(precio)
                    .cantidad(cantidadSolicitada)
                    .build();
            carrito.getItems().add(nuevoItem);
        }

        carrito.setActualizadoEn(LocalDateTime.now());
        carritoRepository.save(carrito);

        return toCarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(String email, Long itemId, ActualizarCantidadRequest request) {
        Usuario usuario = findUsuario(email);
        Carrito carrito = getOrCreateCarrito(usuario);

        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado en el carrito: " + itemId));

        int stock = item.getPrecio().getProduct().getStock();
        if (request.getCantidad() > stock) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + stock);
        }

        item.setCantidad(request.getCantidad());
        carrito.setActualizadoEn(LocalDateTime.now());
        carritoRepository.save(carrito);

        return toCarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(String email, Long itemId) {
        Usuario usuario = findUsuario(email);
        Carrito carrito = getOrCreateCarrito(usuario);

        boolean removed = carrito.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new EntityNotFoundException("Item no encontrado en el carrito: " + itemId);
        }

        carrito.setActualizadoEn(LocalDateTime.now());
        carritoRepository.save(carrito);

        return toCarritoResponse(carrito);
    }

    @Transactional
    public void vaciarCarrito(String email) {
        Usuario usuario = findUsuario(email);
        Carrito carrito = getOrCreateCarrito(usuario);
        carrito.getItems().clear();
        carrito.setActualizadoEn(LocalDateTime.now());
        carritoRepository.save(carrito);
    }

    // --- Helpers ---

    private CarritoResponse toCarritoResponse(Carrito carrito) {
        List<CarritoItemResponse> itemsResponse = carrito.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal subtotal = itemsResponse.stream()
                .map(CarritoItemResponse::getSubtotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CarritoResponse.builder()
                .id(carrito.getId())
                .items(itemsResponse)
                .subtotal(subtotal)
                .total(subtotal) // Sin costo de envío en carrito; se calcula al crear pedido
                .build();
    }

    private CarritoItemResponse toItemResponse(CarritoItem item) {
        Precio precio = item.getPrecio();
        Product producto = precio.getProduct();
        BigDecimal subtotalItem = precio.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));

        return CarritoItemResponse.builder()
                .id(item.getId())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .nombreSecundario(producto.getNombreSecundario())
                .imagenUrl(producto.getImagenUrl())
                .peso(precio.getPeso())
                .precioUnitario(precio.getPrecio())
                .cantidad(item.getCantidad())
                .subtotalItem(subtotalItem)
                .build();
    }
}
