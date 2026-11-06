package utnfc.isi.back.taller.vehiculos.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Permite al jefe de taller corregir marca, modelo o año (por ejemplo, para resolver RN-04). */
public record VehiculoUpdateRequest(
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull @Min(1950) @Max(2100) Integer anio) {
}
