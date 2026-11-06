package utnfc.isi.back.taller.ordenes.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PrecioRepuestoRequest(
        @NotNull Long repuestoId,
        @NotNull @DecimalMin("0.0") BigDecimal precioUnitario) {
}
