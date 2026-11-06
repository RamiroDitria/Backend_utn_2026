package utnfc.isi.back.taller.ordenes.dtos;

import java.time.LocalDateTime;
import java.util.List;

import utnfc.isi.back.taller.ordenes.models.OrdenTrabajo;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;

public record OrdenResponse(
        Long id,
        Long vehiculoId,
        String mecanicoAsignado,
        EstadoOrden estado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaEntrega,
        List<DesperfectoResponse> desperfectos,
        List<RepuestoResponse> repuestos,
        List<PresupuestoResponse> presupuestos) {

    public static OrdenResponse from(OrdenTrabajo o) {
        return new OrdenResponse(o.getId(), o.getVehiculoId(), o.getMecanicoAsignado(), o.getEstado(),
                o.getFechaApertura(), o.getFechaEntrega(),
                o.getDesperfectos().stream().map(DesperfectoResponse::from).toList(),
                o.getRepuestos().stream().map(RepuestoResponse::from).toList(),
                o.getPresupuestos().stream().map(PresupuestoResponse::from).toList());
    }
}
