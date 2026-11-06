package utnfc.isi.back.taller.ordenes.dtos;

import jakarta.validation.constraints.NotBlank;

/** motor y combustible se usan solo si vPIC no reconoce el VIN (carga manual). */
public record VinRequest(
        @NotBlank(message = "El VIN es obligatorio") String vin,
        String motor,
        String combustible) {
}
