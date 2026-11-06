package utnfc.isi.back.taller.notificaciones.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import utnfc.isi.back.taller.notificaciones.models.enums.TipoNotificacion;

public record NotificacionRequest(
        @NotNull(message = "El tipo es obligatorio") TipoNotificacion tipo,
        @NotNull(message = "La orden es obligatoria") Long ordenId,
        @NotBlank(message = "El teléfono es obligatorio") String telefono,
        String nombreCliente,
        BigDecimal monto) {
}
