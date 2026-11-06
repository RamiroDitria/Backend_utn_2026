package utnfc.isi.back.taller.ordenes.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.commons.security.TokenUtils;
import utnfc.isi.back.taller.ordenes.clients.VehiculosClient;
import utnfc.isi.back.taller.ordenes.dtos.DecisionRequest;
import utnfc.isi.back.taller.ordenes.dtos.PrecioRepuestoRequest;
import utnfc.isi.back.taller.ordenes.dtos.PresupuestoRequest;
import utnfc.isi.back.taller.ordenes.models.Desperfecto;
import utnfc.isi.back.taller.ordenes.models.OrdenTrabajo;
import utnfc.isi.back.taller.ordenes.models.Presupuesto;
import utnfc.isi.back.taller.ordenes.models.RepuestoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.DecisionPresupuesto;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoRepuesto;
import utnfc.isi.back.taller.ordenes.models.enums.TipoPresupuesto;

@Service
public class PresupuestoService {

    private final OrdenTrabajoService ordenService;
    private final VehiculosClient vehiculosClient;
    private final EntityManager entityManager;
    private final int diasVigencia;

    public PresupuestoService(OrdenTrabajoService ordenService, VehiculosClient vehiculosClient,
                              EntityManager entityManager,
                              @Value("${presupuesto.dias-vigencia:7}") int diasVigencia) {
        this.ordenService = ordenService;
        this.vehiculosClient = vehiculosClient;
        this.entityManager = entityManager;
        this.diasVigencia = diasVigencia;
    }

    /**
     * F06 - Generar presupuesto (inicial o adicional).
     * RN-04: no se presupuesta un vehículo con marca inconsistente.
     * RN-06: total = repuestos + mano de obra + otros cargos; requiere desperfectos; vence a los N días.
     */
    @Transactional
    public OrdenTrabajo generar(Long ordenId, PresupuestoRequest request) {
        OrdenTrabajo orden = ordenService.obtener(ordenId);
        LocalDate hoy = LocalDate.now();

        Optional<Presupuesto> pendiente = orden.presupuestoPendiente();
        if (pendiente.isPresent()) {
            if (!pendiente.get().estaVencido(hoy)) {
                throw ServiceException.conflicto("RN-06", "Ya hay un presupuesto vigente esperando la decisión del cliente");
            }
            pendiente.get().setDecision(DecisionPresupuesto.VENCIDO);
        }

        TipoPresupuesto tipo = switch (orden.getEstado()) {
            case EN_DIAGNOSTICO, PRESUPUESTADA -> TipoPresupuesto.INICIAL;
            case APROBACION_ADICIONAL -> TipoPresupuesto.ADICIONAL;
            default -> throw ServiceException.conflicto("ESTADO_INVALIDO",
                    "No se puede presupuestar una orden en estado " + orden.getEstado());
        };
        boolean adicional = tipo == TipoPresupuesto.ADICIONAL;

        if (vehiculosClient.obtener(orden.getVehiculoId()).marcaInconsistente()) {
            throw ServiceException.reglaNegocio("RN-04",
                    "La marca registrada no coincide con la del VIN. Corregir el vehículo antes de presupuestar");
        }

        List<Desperfecto> desperfectos = orden.getDesperfectos().stream()
                .filter(d -> d.isAdicional() == adicional && sinDecision(d.getPresupuesto()))
                .toList();
        List<RepuestoOrden> repuestos = orden.getRepuestos().stream()
                .filter(r -> r.isAdicional() == adicional && sinDecision(r.getPresupuesto()))
                .filter(r -> r.getEstado() != EstadoRepuesto.DESCARTADO)
                .toList();

        if (tipo == TipoPresupuesto.INICIAL && desperfectos.isEmpty()) {
            throw ServiceException.reglaNegocio("RN-06", "El presupuesto requiere al menos un desperfecto registrado");
        }
        if (desperfectos.isEmpty() && repuestos.isEmpty()) {
            throw ServiceException.reglaNegocio("RN-06", "No hay desperfectos ni repuestos nuevos para presupuestar");
        }

        Map<Long, BigDecimal> precios = request.preciosRepuestos() == null ? Map.of()
                : request.preciosRepuestos().stream()
                        .collect(Collectors.toMap(PrecioRepuestoRequest::repuestoId,
                                PrecioRepuestoRequest::precioUnitario, (a, b) -> b));

        BigDecimal montoRepuestos = BigDecimal.ZERO;
        for (RepuestoOrden repuesto : repuestos) {
            BigDecimal precio = precios.get(repuesto.getId());
            if (precio == null) {
                throw ServiceException.reglaNegocio("RN-06", "Falta el precio del repuesto " + repuesto.getId());
            }
            repuesto.setPrecioUnitario(precio);
            montoRepuestos = montoRepuestos.add(repuesto.subtotal());
        }
        BigDecimal manoObra = request.horasManoObra().multiply(request.tarifaHora());
        BigDecimal otros = request.otrosCargos() == null ? BigDecimal.ZERO : request.otrosCargos();

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setTipo(tipo);
        presupuesto.setMontoRepuestos(montoRepuestos);
        presupuesto.setHorasManoObra(request.horasManoObra());
        presupuesto.setTarifaHora(request.tarifaHora());
        presupuesto.setMontoManoObra(manoObra);
        presupuesto.setOtrosCargos(otros);
        presupuesto.setTotal(montoRepuestos.add(manoObra).add(otros));
        presupuesto.setFechaEmision(hoy);
        presupuesto.setFechaVencimiento(hoy.plusDays(diasVigencia));
        presupuesto.setDecision(DecisionPresupuesto.PENDIENTE);
        orden.agregarPresupuesto(presupuesto);
        if (orden.getEstado() == EstadoOrden.EN_DIAGNOSTICO) {
            orden.cambiarEstado(EstadoOrden.PRESUPUESTADA);
        }
        // El presupuesto usa id IDENTITY: hay que insertarlo (y tener su id) antes de poder
        // asignarlo como FK en desperfectos/repuestos que ya son entidades persistidas; si no,
        // Hibernate rechaza el flush (TransientPropertyValueException).
        entityManager.persist(presupuesto);
        entityManager.flush();

        desperfectos.forEach(d -> d.setPresupuesto(presupuesto));
        repuestos.forEach(r -> r.setPresupuesto(presupuesto));
        OrdenTrabajo guardada = ordenService.guardar(orden);
        ordenService.notificarCliente(guardada, "PRESUPUESTO", presupuesto.getTotal());
        return guardada;
    }

    /**
     * F07 - Registrar la decisión del cliente.
     * RN-06: no se acepta un presupuesto vencido. RN-08: el adicional define si se sigue o se esperan repuestos.
     */
    @Transactional
    public OrdenTrabajo registrarDecision(Long ordenId, Long presupuestoId, DecisionRequest request) {
        OrdenTrabajo orden = ordenService.obtener(ordenId);
        Presupuesto presupuesto = orden.getPresupuestos().stream()
                .filter(p -> p.getId().equals(presupuestoId))
                .findFirst()
                .orElseThrow(() -> ServiceException.noEncontrado("PRESUPUESTO_NO_ENCONTRADO",
                        "La orden no tiene el presupuesto " + presupuestoId));

        if (presupuesto.getDecision() != DecisionPresupuesto.PENDIENTE) {
            throw ServiceException.conflicto("RN-06", "El presupuesto ya tiene una decisión registrada");
        }
        if (presupuesto.estaVencido(LocalDate.now())) {
            throw ServiceException.reglaNegocio("RN-06", "El presupuesto está vencido; hay que generar uno nuevo");
        }

        boolean aceptado = request.aceptado();
        presupuesto.setDecision(aceptado ? DecisionPresupuesto.ACEPTADO : DecisionPresupuesto.RECHAZADO);
        presupuesto.setCanalDecision(request.canal().toUpperCase());
        presupuesto.setRegistradaPor(TokenUtils.usuarioActual());
        presupuesto.setFechaDecision(LocalDateTime.now());

        List<RepuestoOrden> repuestosDelPresupuesto = orden.getRepuestos().stream()
                .filter(r -> r.getPresupuesto() == presupuesto)
                .toList();
        if (!aceptado) {
            repuestosDelPresupuesto.forEach(r -> r.setEstado(EstadoRepuesto.DESCARTADO));
        }

        if (presupuesto.getTipo() == TipoPresupuesto.INICIAL) {
            if (aceptado) {
                orden.cambiarEstado(EstadoOrden.ACEPTADA);
            } else {
                // Rechazo del presupuesto inicial: se devuelve el vehículo sin reparar.
                // La orden avanza sola a Lista para retirar (no espera un F10 que nunca va a llegar);
                // igual queda pendiente el F11 (entregar) para cerrarla en Entregada.
                orden.cambiarEstado(EstadoOrden.RECHAZADA);
                orden.cambiarEstado(EstadoOrden.LISTA_PARA_RETIRAR);
                ordenService.notificarCliente(orden, "VEHICULO_LISTO", null);
            }
        } else {
            boolean esperarRepuestos = aceptado && !repuestosDelPresupuesto.isEmpty();
            orden.cambiarEstado(esperarRepuestos ? EstadoOrden.ESPERANDO_REPUESTOS : EstadoOrden.EN_REPARACION);
        }
        return orden;
    }

    private boolean sinDecision(Presupuesto presupuesto) {
        return presupuesto == null
                || presupuesto.getDecision() == DecisionPresupuesto.PENDIENTE
                || presupuesto.getDecision() == DecisionPresupuesto.VENCIDO;
    }
}
