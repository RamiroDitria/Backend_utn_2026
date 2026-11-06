package utnfc.isi.back.taller.ordenes.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.ordenes.models.enums.DecisionPresupuesto;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoRepuesto;

@Entity
@Table(name = "ordenes_trabajo")
@Getter
@Setter
@NoArgsConstructor
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id del vehículo en ms-clientes-vehiculos (no se comparte la entidad entre servicios). */
    @Column(nullable = false)
    private Long vehiculoId;

    /** Usuario de Keycloak (preferred_username) del mecánico asignado. */
    @Column(nullable = false, length = 50)
    private String mecanicoAsignado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaEntrega;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Desperfecto> desperfectos = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<RepuestoOrden> repuestos = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Presupuesto> presupuestos = new ArrayList<>();

    /** Cambia de estado solo si la transición está permitida. */
    public void cambiarEstado(EstadoOrden destino) {
        if (!estado.puedePasarA(destino)) {
            throw ServiceException.conflicto("TRANSICION_INVALIDA",
                    "La orden no puede pasar de " + estado + " a " + destino);
        }
        this.estado = destino;
    }

    public void agregarDesperfecto(Desperfecto desperfecto) {
        desperfecto.setOrden(this);
        desperfectos.add(desperfecto);
    }

    public void agregarRepuesto(RepuestoOrden repuesto) {
        repuesto.setOrden(this);
        repuestos.add(repuesto);
    }

    public void agregarPresupuesto(Presupuesto presupuesto) {
        presupuesto.setOrden(this);
        presupuestos.add(presupuesto);
    }

    public Optional<Presupuesto> presupuestoPendiente() {
        return presupuestos.stream().filter(p -> p.getDecision() == DecisionPresupuesto.PENDIENTE).findFirst();
    }

    /** Repuestos de presupuestos aceptados que todavía no se pidieron. */
    public List<RepuestoOrden> repuestosAceptadosSinPedir() {
        return repuestos.stream()
                .filter(RepuestoOrden::estaAceptado)
                .filter(r -> r.getEstado() == EstadoRepuesto.PENDIENTE)
                .toList();
    }

    /** RN-07 / RN-10: hay repuestos aceptados que no llegaron. */
    public boolean tieneRepuestosAceptadosSinRecibir() {
        return repuestos.stream()
                .filter(RepuestoOrden::estaAceptado)
                .anyMatch(r -> r.getEstado() == EstadoRepuesto.PENDIENTE || r.getEstado() == EstadoRepuesto.PEDIDO);
    }
}
