package pe.valleblanco.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class Requests {
    private Requests() {
    }

    public record CategoriaInput(@NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion) {
    }

    public record ProductoInput(@NotNull Long categoriaId, @NotBlank @Size(max = 150) String nombre, String descripcion,
            @NotNull @DecimalMin("0.01") BigDecimal precio, @NotNull @Min(0) Integer stock,
            @Size(max = 100) String tipoPreparado, String imagenUrl, Boolean destacado, Boolean estado) {
    }

    public record Registro(@NotBlank @Size(max = 120) String nombre, @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record Login(@NotBlank String email, @NotBlank String password) {
    }

    public record CarritoInput(@NotNull @Positive Long productoId, @NotNull @Min(1) Integer cantidad) {
    }

    public record CantidadInput(@NotNull @Min(1) Integer cantidad) {
    }

    public record PedidoInput(@NotBlank String metodoPago, @NotBlank @Size(max = 500) String direccionEnvio) {
    }
}
