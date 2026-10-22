
package pe.valleblanco.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import pe.valleblanco.backend.dto.Requests;
import pe.valleblanco.backend.model.Categoria;
import pe.valleblanco.backend.model.Producto;
import pe.valleblanco.backend.repo.CategoriaRepo;
import pe.valleblanco.backend.repo.ProductoRepo;
import pe.valleblanco.backend.service.Autenticacion;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final ProductoRepo productos;
    private final CategoriaRepo categorias;
    private final Autenticacion auth;

    public CatalogoController(
            ProductoRepo p,
            CategoriaRepo c,
            Autenticacion a) {
        this.productos = p;
        this.categorias = c;
        this.auth = a;
    }

    // LISTAR CATEGORÍAS
    @GetMapping("/categorias")
    public List<Categoria> categorias() {
        return categorias.findAll();
    }

    // LISTAR PRODUCTOS CON FILTROS
    @GetMapping("/productos")
    public List<Producto> productos(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String buscar) {

        String busqueda = buscar == null ? "" : buscar.trim();

        if (categoria == null) {
            return productos.catalogo(busqueda);
        }

        return productos.catalogoPorCategoria(
                categoria,
                busqueda);
    }

    // PRODUCTOS DESTACADOS
    @GetMapping("/productos/destacados")
    public List<Producto> destacados() {
        return productos.findByEstadoTrueAndDestacadoTrue();
    }

    // DETALLE DE PRODUCTO
    @GetMapping("/productos/{id}")
    public Producto detalle(@PathVariable Long id) {

        Producto p = productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));

        if (!p.estado) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado");
        }

        return p;
    }

    // REGISTRAR CATEGORÍA
    @PostMapping("/categorias")
    public Categoria categoria(
            @RequestHeader(value = "Authorization", required = false) String h,
            @Valid @RequestBody Requests.CategoriaInput r) {

        auth.admin(h);

        return categorias.save(
                new Categoria(
                        r.nombre().trim(),
                        r.descripcion()));
    }

    // REGISTRAR PRODUCTO
    @PostMapping("/productos")
    public Producto crear(
            @RequestHeader(value = "Authorization", required = false) String h,
            @Valid @RequestBody Requests.ProductoInput r) {

        auth.admin(h);

        return productos.save(
                aplicar(new Producto(), r));
    }

    // ACTUALIZAR PRODUCTO
    @PutMapping("/productos/{id}")
    public Producto actualizar(
            @RequestHeader(value = "Authorization", required = false) String h,
            @PathVariable Long id,
            @Valid @RequestBody Requests.ProductoInput r) {

        auth.admin(h);

        Producto p = productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));

        return productos.save(aplicar(p, r));
    }

    // VALIDAR Y ASIGNAR DATOS
    private Producto aplicar(
            Producto p,
            Requests.ProductoInput r) {

        p.categoria = categorias.findById(r.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Categoría no existe"));

        p.nombre = r.nombre().trim();
        p.descripcion = r.descripcion();
        p.precio = r.precio();
        p.stock = r.stock();
        p.tipoPreparado = r.tipoPreparado();
        p.imagenUrl = r.imagenUrl();
        p.destacado = Boolean.TRUE.equals(r.destacado());
        p.estado = r.estado() == null || r.estado();

        return p;
    }
}
