package utnfc.isi.back.taller.ordenes.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utnfc.isi.back.taller.ordenes.models.enums.DecisionPresupuesto;
import utnfc.isi.back.taller.ordenes.models.enums.TipoPresupuesto;

@Entity
@Table(name = "presupuestos")
@Getter
@Setter
@NoArgsConstructor
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoPresupuesto tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoRepuestos;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal horasManoObra;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaHora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoManoObra;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal otrosCargos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DecisionPresupuesto decision;

    /** Canal por el que el cliente comunicó la decisión (WhatsApp, presencial, teléfono). */
    @Column(length = 20)
    private String canalDecision;

    /** Usuario que registró la decisión. */
    @Column(length = 50)
    private String registradaPor;

    private LocalDateTime fechaDecision;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id")
    private OrdenTrabajo orden;

    /** RN-06: vence a los N días de emitido si no tiene decisión. */
    public boolean estaVencido(LocalDate hoy) {
        return decision == DecisionPresupuesto.PENDIENTE && hoy.isAfter(fechaVencimiento);
    }
}
