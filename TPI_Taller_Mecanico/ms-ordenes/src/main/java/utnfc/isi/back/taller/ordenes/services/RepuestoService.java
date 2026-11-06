package utnfc.isi.back.taller.ordenes.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.ordenes.dtos.RepuestoRequest;
import utnfc.isi.back.taller.ordenes.models.OrdenTrabajo;
import utnfc.isi.back.taller.ordenes.models.RepuestoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoRepuesto;

@Service
@RequiredArgsConstructor
public class RepuestoService {

    private final OrdenTrabajoService ordenService;

    /** F05 - Registrar repuesto necesario (y F09 si la orden está en reparación). RN-03. */
    @Transactional
    public OrdenTrabajo agregar(Long ordenId, RepuestoRequest request) {
        OrdenTrabajo orden = ordenService.obtener(ordenId);
        ordenService.validarMecanicoAsignado(orden);
        boolean adicional = ordenService.prepararCarga(orden);

        RepuestoOrden repuesto = new RepuestoOrden();
        repuesto.setDescripcion(request.descripcion());
        repuesto.setCantidad(request.cantidad());
        repuesto.setEstado(EstadoRepuesto.PENDIENTE);
        repuesto.setAdicional(adicional);
        orden.agregarRepuesto(repuesto);
        return ordenService.guardar(orden);
    }

    /**
     * F08 - Registrar el pedido de los repuestos aceptados. RN-07.
     * Si no hay repuestos que pedir, la orden pasa directo a reparación.
     */
    @Transactional
    public OrdenTrabajo registrarPedido(Long ordenId) {
        OrdenTrabajo orden = ordenService.obtener(ordenId);
        if (orden.getEstado() != EstadoOrden.ACEPTADA && orden.getEstado() != EstadoOrden.ESPERANDO_REPUESTOS) {
            throw ServiceException.reglaNegocio("RN-07", "Solo se piden repuestos con el presupuesto aceptado");
        }
        List<RepuestoOrden> sinPedir = orden.repuestosAceptadosSinPedir();
        sinPedir.forEach(r -> r.setEstado(EstadoRepuesto.PEDIDO));

        if (!orden.tieneRepuestosAceptadosSinRecibir()) {
            orden.cambiarEstado(EstadoOrden.EN_REPARACION);
        } else if (orden.getEstado() == EstadoOrden.ACEPTADA) {
            orden.cambiarEstado(EstadoOrden.ESPERANDO_REPUESTOS);
        }
        return orden;
    }

    /** F08 - Registrar la llegada de un repuesto. RN-07: con todos recibidos pasa a reparación. */
    @Transactional
    public OrdenTrabajo registrarRecepcion(Long ordenId, Long repuestoId) {
        OrdenTrabajo orden = ordenService.obtener(ordenId);
        if (orden.getEstado() != EstadoOrden.ESPERANDO_REPUESTOS) {
            throw ServiceException.conflicto("ESTADO_INVALIDO", "La orden no está esperando repuestos");
        }
        RepuestoOrden repuesto = orden.getRepuestos().stream()
                .filter(r -> r.getId().equals(repuestoId))
                .findFirst()
                .orElseThrow(() -> ServiceException.noEncontrado("REPUESTO_NO_ENCONTRADO",
                        "La orden no tiene el repuesto " + repuestoId));
        if (repuesto.getEstado() != EstadoRepuesto.PEDIDO) {
            throw ServiceException.conflicto("REPUESTO_NO_PEDIDO", "El repuesto no está pedido");
        }
        repuesto.setEstado(EstadoRepuesto.RECIBIDO);
        if (!orden.tieneRepuestosAceptadosSinRecibir()) {
            orden.cambiarEstado(EstadoOrden.EN_REPARACION);
        }
        return orden;
    }
}
