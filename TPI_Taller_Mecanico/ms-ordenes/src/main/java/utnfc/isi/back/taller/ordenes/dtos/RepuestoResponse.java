package utnfc.isi.back.taller.ordenes.dtos;

import java.math.BigDecimal;

import utnfc.isi.back.taller.ordenes.models.RepuestoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoRepuesto;

public record RepuestoResponse(
        Long id, String descripcion, Integer cantidad, BigDecimal precioUnitario,
        EstadoRepuesto estado, boolean adicional, Long presupuestoId) {

    public static RepuestoResponse from(RepuestoOrden r) {
        return new RepuestoResponse(r.getId(), r.getDescripcion(), r.getCantidad(), r.getPrecioUnitario(),
                r.getEstado(), r.isAdicional(), r.getPresupuesto() == null ? null : r.getPresupuesto().getId());
    }
}
