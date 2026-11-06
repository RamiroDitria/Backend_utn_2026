package utnfc.isi.back.taller.ordenes.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** El jefe de taller registra lo que decidió el cliente y por qué canal lo comunicó. */
public record DecisionRequest(
        @NotNull(message = "Indicar si el cliente aceptó") Boolean aceptado,
        @NotBlank(message = "El canal es obligatorio (WHATSAPP, PRESENCIAL, TELEFONO)") @Size(max = 20) String canal) {
}
