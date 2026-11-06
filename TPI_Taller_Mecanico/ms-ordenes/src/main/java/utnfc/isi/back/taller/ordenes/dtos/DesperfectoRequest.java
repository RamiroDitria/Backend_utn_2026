package utnfc.isi.back.taller.ordenes.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import utnfc.isi.back.taller.ordenes.models.enums.OrigenDesperfecto;

public record DesperfectoRequest(
        @NotNull(message = "El origen es obligatorio (ESCANER o MANUAL)") OrigenDesperfecto origen,
        String codigoObd,
        @Size(max = 500) String descripcion) {
}
