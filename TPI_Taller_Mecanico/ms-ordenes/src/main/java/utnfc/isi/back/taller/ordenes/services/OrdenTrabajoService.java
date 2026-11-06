package utnfc.isi.back.taller.ordenes.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.commons.security.TokenUtils;
import utnfc.isi.back.taller.commons.validations.VinValidator;
import utnfc.isi.back.taller.ordenes.clients.NotificacionesClient;
import utnfc.isi.back.taller.ordenes.clients.VehiculosClient;
import utnfc.isi.back.taller.ordenes.dtos.AbrirOrdenRequest;
import utnfc.isi.back.taller.ordenes.dtos.DesperfectoRequest;
import utnfc.isi.back.taller.ordenes.dtos.NotificacionRequest;
import utnfc.isi.back.taller.ordenes.dtos.VehiculoDTO;
import utnfc.isi.back.taller.ordenes.dtos.VinRequest;
import utnfc.isi.back.taller.ordenes.models.Desperfecto;
import utnfc.isi.back.taller.ordenes.models.OrdenTrabajo;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.OrigenDesperfecto;
import utnfc.isi.back.taller.ordenes.repositories.OrdenTrabajoRepository;

@Service
@RequiredArgsConstructor
public class OrdenTrabajoService {

    private static final Logger log = LoggerFactory.getLogger(OrdenTrabajoService.class);
    private static final Pattern CODIGO_OBD = Pattern.compile("^[PCBU][0-3][0-9A-F]{3}$");
    private static final String ROL_JEFE = "JEFE_TALLER";

    private final OrdenTrabajoRepository ordenRepository;
    private final VehiculosClient vehiculosClient;
    private final NotificacionesClient notificacionesClient;

    /** F02 - Abrir OT y asignar mecánico. RN-02: un vehículo no puede tener dos OT abiertas. */
    @Transactional
    public OrdenTrabajo abrir(AbrirOrdenRequest request) {
        VehiculoDTO vehiculo = vehiculosClient.obtener(request.vehiculoId());
        if (ordenRepository.existsByVehiculoIdAndEstadoNot(vehiculo.id(), EstadoOrden.ENTREGADA)) {
            throw ServiceException.conflicto("RN-02", "El vehículo " + vehiculo.patente() + " ya tiene una orden abierta");
        }
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculoId(vehiculo.id());
        orden.setMecanicoAsignado(request.mecanicoAsignado());
        orden.setEstado(EstadoOrden.RECIBIDA);
        orden.setFechaApertura(LocalDateTime.now());
        return ordenRepository.save(orden);
    }

    public OrdenTrabajo obtener(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> ServiceException.noEncontrado("OT_NO_ENCONTRADA", "No existe la orden " + id));
    }

    /** El mecánico solo puede consultar las órdenes que tiene asignadas. */
    public OrdenTrabajo obtenerParaConsulta(Long id) {
        OrdenTrabajo orden = obtener(id);
        if (!TokenUtils.tieneRol(ROL_JEFE)) {
            validarMecanicoAsignado(orden);
        }
        return orden;
    }

    public List<OrdenTrabajo> listar(EstadoOrden estado) {
        if (TokenUtils.tieneRol(ROL_JEFE)) {
            return estado == null ? ordenRepository.findAll() : ordenRepository.findByEstado(estado);
        }
        String mecanico = TokenUtils.usuarioActual();
        return estado == null
                ? ordenRepository.findByMecanicoAsignado(mecanico)
                : ordenRepository.findByMecanicoAsignadoAndEstado(mecanico, estado);
    }

    /** F03 - Registrar VIN. RN-03 y RN-04. La consulta a vPIC la hace ms-clientes-vehiculos. */
    @Transactional
    public OrdenTrabajo registrarVin(Long id, VinRequest request) {
        OrdenTrabajo orden = obtener(id);
        validarMecanicoAsignado(orden);
        if (orden.getEstado() != EstadoOrden.RECIBIDA && orden.getEstado() != EstadoOrden.EN_DIAGNOSTICO) {
            throw ServiceException.conflicto("ESTADO_INVALIDO", "El VIN se registra al iniciar el diagnóstico");
        }
        if (!VinValidator.esValido(request.vin())) {
            throw ServiceException.reglaNegocio("RN-04", "El VIN debe tener 17 caracteres y no puede contener I, O ni Q");
        }
        vehiculosClient.registrarVin(orden.getVehiculoId(), request);
        if (orden.getEstado() == EstadoOrden.RECIBIDA) {
            orden.cambiarEstado(EstadoOrden.EN_DIAGNOSTICO);
        }
        return orden;
    }

    /**
     * F04 - Registrar desperfecto (y F09 si la orden está en reparación). RN-03 y RN-05.
     */
    @Transactional
    public OrdenTrabajo agregarDesperfecto(Long id, DesperfectoRequest request) {
        OrdenTrabajo orden = obtener(id);
        validarMecanicoAsignado(orden);
        validarDesperfecto(request);
        boolean adicional = prepararCarga(orden);

        Desperfecto desperfecto = new Desperfecto();
        desperfecto.setOrigen(request.origen());
        desperfecto.setCodigoObd(request.codigoObd() == null ? null : request.codigoObd().trim().toUpperCase());
        desperfecto.setDescripcion(request.descripcion());
        desperfecto.setAdicional(adicional);
        desperfecto.setFechaRegistro(LocalDateTime.now());
        orden.agregarDesperfecto(desperfecto);
        return ordenRepository.saveAndFlush(orden);
    }

    /** F10 - Finalizar trabajo. RN-03 y RN-10. */
    @Transactional
    public OrdenTrabajo finalizar(Long id) {
        OrdenTrabajo orden = obtener(id);
        validarMecanicoAsignado(orden);
        if (orden.getEstado() == EstadoOrden.APROBACION_ADICIONAL) {
            throw ServiceException.reglaNegocio("RN-10", "Hay un adicional sin decisión del cliente");
        }
        if (orden.getEstado() != EstadoOrden.EN_REPARACION) {
            throw ServiceException.reglaNegocio("RN-10", "Solo se puede finalizar una orden en reparación");
        }
        if (orden.tieneRepuestosAceptadosSinRecibir()) {
            throw ServiceException.reglaNegocio("RN-10", "Hay repuestos aceptados que todavía no llegaron");
        }
        orden.cambiarEstado(EstadoOrden.LISTA_PARA_RETIRAR);
        notificarCliente(orden, "VEHICULO_LISTO", null);
        return orden;
    }

    /** F11 - Registrar entrega. RN-10. */
    @Transactional
    public OrdenTrabajo entregar(Long id) {
        OrdenTrabajo orden = obtener(id);
        if (orden.getEstado() != EstadoOrden.LISTA_PARA_RETIRAR) {
            throw ServiceException.reglaNegocio("RN-10", "Solo se puede entregar un vehículo listo para retirar");
        }
        orden.cambiarEstado(EstadoOrden.ENTREGADA);
        orden.setFechaEntrega(LocalDateTime.now());
        return orden;
    }

    // ---------- métodos que también usan los otros servicios ----------

    /** RN-03: solo el mecánico asignado puede cargar información en la orden. */
    public void validarMecanicoAsignado(OrdenTrabajo orden) {
        String usuario = TokenUtils.usuarioActual();
        if (usuario == null || !usuario.equals(orden.getMecanicoAsignado())) {
            throw ServiceException.prohibido("RN-03", "Solo el mecánico asignado puede operar sobre esta orden");
        }
    }

    /**
     * Decide si un desperfecto o repuesto nuevo es parte del diagnóstico inicial
     * o un adicional que aparece durante la reparación (RN-08).
     * Devuelve true si es adicional.
     */
    public boolean prepararCarga(OrdenTrabajo orden) {
        return switch (orden.getEstado()) {
            case EN_DIAGNOSTICO -> false;
            case EN_REPARACION -> {
                orden.cambiarEstado(EstadoOrden.APROBACION_ADICIONAL);
                yield true;
            }
            case APROBACION_ADICIONAL -> {
                if (orden.presupuestoPendiente().isPresent()) {
                    throw ServiceException.conflicto("RN-08",
                            "Ya hay un presupuesto adicional esperando la decisión del cliente");
                }
                yield true;
            }
            default -> throw ServiceException.conflicto("ESTADO_INVALIDO",
                    "No se pueden cargar desperfectos ni repuestos en estado " + orden.getEstado());
        };
    }

    /** RN-09: el aviso por WhatsApp nunca frena la orden. */
    public void notificarCliente(OrdenTrabajo orden, String tipo, BigDecimal monto) {
        try {
            VehiculoDTO vehiculo = vehiculosClient.obtener(orden.getVehiculoId());
            notificacionesClient.enviar(new NotificacionRequest(
                    tipo, orden.getId(), vehiculo.clienteTelefono(), vehiculo.clienteNombre(), monto));
        } catch (ServiceException e) {
            log.warn("No se pudo notificar la orden {}: {}", orden.getId(), e.getMessage());
        }
    }

    public OrdenTrabajo guardar(OrdenTrabajo orden) {
        return ordenRepository.saveAndFlush(orden);
    }

    /** RN-05: código OBD-II válido si viene del escáner; descripción si es manual. */
    private void validarDesperfecto(DesperfectoRequest request) {
        if (request.origen() == OrigenDesperfecto.ESCANER) {
            String codigo = request.codigoObd() == null ? "" : request.codigoObd().trim().toUpperCase();
            if (!CODIGO_OBD.matcher(codigo).matches()) {
                throw ServiceException.reglaNegocio("RN-05", "El código OBD-II debe tener el formato P0301 (P, C, B o U + 4 caracteres)");
            }
        } else if (request.descripcion() == null || request.descripcion().isBlank()) {
            throw ServiceException.reglaNegocio("RN-05", "Un desperfecto manual requiere descripción");
        }
    }
}
