package utnfc.isi.back.taller.ordenes.dtos;

import utnfc.isi.back.taller.ordenes.models.Desperfecto;
import utnfc.isi.back.taller.ordenes.models.enums.OrigenDesperfecto;

public record DesperfectoResponse(
        Long id, OrigenDesperfecto origen, String codigoObd, String descripcion, boolean adicional, Long presupuestoId) {

    public static DesperfectoResponse from(Desperfecto d) {
        return new DesperfectoResponse(d.getId(), d.getOrigen(), d.getCodigoObd(), d.getDescripcion(),
                d.isAdicional(), d.getPresupuesto() == null ? null : d.getPresupuesto().getId());
    }
}
