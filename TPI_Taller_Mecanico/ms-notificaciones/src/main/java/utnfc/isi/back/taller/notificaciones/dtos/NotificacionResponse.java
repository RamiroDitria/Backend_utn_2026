package utnfc.isi.back.taller.notificaciones.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import utnfc.isi.back.taller.notificaciones.models.Notificacion;
import utnfc.isi.back.taller.notificaciones.models.enums.EstadoNotificacion;
import utnfc.isi.back.taller.notificaciones.models.enums.TipoNotificacion;

public record NotificacionResponse(
        Long id, Long ordenId, TipoNotificacion tipo, String telefono, BigDecimal monto,
        EstadoNotificacion estado, int intentos, String ultimoError,
        LocalDateTime fechaCreacion, LocalDateTime fechaEnvio) {

    public static NotificacionResponse from(Notificacion n) {
        return new NotificacionResponse(n.getId(), n.getOrdenId(), n.getTipo(), n.getTelefono(), n.getMonto(),
                n.getEstado(), n.getIntentos(), n.getUltimoError(), n.getFechaCreacion(), n.getFechaEnvio());
    }
}
