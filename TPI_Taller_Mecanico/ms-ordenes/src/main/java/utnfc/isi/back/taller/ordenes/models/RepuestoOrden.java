package utnfc.isi.back.taller.ordenes.models;

import java.math.BigDecimal;

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
import utnfc.isi.back.taller.ordenes.models.enums.EstadoRepuesto;

@Entity
@Table(name = "repuestos_orden")
@Getter
@Setter
@NoArgsConstructor
public class RepuestoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad;

    /** Lo fija el jefe de taller al generar el presupuesto. */
    @Column(precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoRepuesto estado;

    private boolean adicional;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id")
    private OrdenTrabajo orden;

    @ManyToOne
    @JoinColumn(name = "presupuesto_id")
    private Presupuesto presupuesto;

    public boolean estaAceptado() {
        return presupuesto != null && presupuesto.getDecision() == DecisionPresupuesto.ACEPTADO;
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
