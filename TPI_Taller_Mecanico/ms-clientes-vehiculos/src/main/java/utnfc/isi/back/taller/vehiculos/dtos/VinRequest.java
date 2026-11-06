package utnfc.isi.back.taller.vehiculos.dtos;

import jakarta.validation.constraints.NotBlank;

/** motor y combustible solo se usan si vPIC no puede decodificar el VIN (carga manual). */
public record VinRequest(
        @NotBlank(message = "El VIN es obligatorio") String vin,
        String motor,
        String combustible) {
}
