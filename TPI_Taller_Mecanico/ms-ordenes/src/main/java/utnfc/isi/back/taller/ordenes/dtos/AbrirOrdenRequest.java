package utnfc.isi.back.taller.ordenes.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AbrirOrdenRequest(
        @NotNull(message = "El vehículo es obligatorio") Long vehiculoId,
        @NotBlank(message = "El mecánico asignado es obligatorio") String mecanicoAsignado) {
}
