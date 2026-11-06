package utnfc.isi.back.taller.ordenes.dtos;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PresupuestoRequest(
        @NotNull(message = "Las horas de mano de obra son obligatorias") @DecimalMin("0.0") BigDecimal horasManoObra,
        @NotNull(message = "La tarifa por hora es obligatoria") @DecimalMin("0.0") BigDecimal tarifaHora,
        @DecimalMin("0.0") BigDecimal otrosCargos,
        @Valid List<PrecioRepuestoRequest> preciosRepuestos) {
}
