package utnfc.isi.back.taller.ordenes.models;

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
import utnfc.isi.back.taller.ordenes.models.enums.OrigenDesperfecto;

@Entity
@Table(name = "desperfectos")
@Getter
@Setter
@NoArgsConstructor
public class Desperfecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigenDesperfecto origen;

    /** Código OBD-II leído con el escáner (ej: P0301). */
    @Column(length = 5)
    private String codigoObd;

    @Column(length = 500)
    private String descripcion;

    /** true si se agregó durante la reparación (genera presupuesto adicional). */
    private boolean adicional;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id")
    private OrdenTrabajo orden;

    /** Presupuesto en el que se incluyó; null mientras no se presupuestó. */
    @ManyToOne
    @JoinColumn(name = "presupuesto_id")
    private Presupuesto presupuesto;
}
