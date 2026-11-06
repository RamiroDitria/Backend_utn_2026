package utnfc.isi.back.taller.ordenes.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RepuestoRequest(
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 150) String descripcion,
        @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad mínima es 1") Integer cantidad) {
}
