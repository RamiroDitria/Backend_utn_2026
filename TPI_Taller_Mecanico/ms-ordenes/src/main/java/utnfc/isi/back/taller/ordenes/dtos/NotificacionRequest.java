package utnfc.isi.back.taller.ordenes.dtos;

import java.math.BigDecimal;

/** tipo: PRESUPUESTO o VEHICULO_LISTO. */
public record NotificacionRequest(
        String tipo, Long ordenId, String telefono, String nombreCliente, BigDecimal monto) {
}
