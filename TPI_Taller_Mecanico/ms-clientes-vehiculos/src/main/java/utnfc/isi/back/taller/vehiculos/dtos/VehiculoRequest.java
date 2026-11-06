package utnfc.isi.back.taller.vehiculos.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculoRequest(
        @NotBlank(message = "La patente es obligatoria") String patente,
        @NotBlank(message = "La marca es obligatoria") String marca,
        @NotBlank(message = "El modelo es obligatorio") String modelo,
        @NotNull(message = "El año es obligatorio") @Min(1950) @Max(2100) Integer anio,
        @NotNull(message = "El cliente es obligatorio") Long clienteId) {
}
