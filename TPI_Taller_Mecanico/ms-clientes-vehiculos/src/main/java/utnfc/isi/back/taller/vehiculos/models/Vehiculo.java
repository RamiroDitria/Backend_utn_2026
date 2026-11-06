package utnfc.isi.back.taller.vehiculos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String patente;

    /** Marca, modelo y año que registra el jefe de taller al dar de alta el vehículo. */
    @Column(nullable = false, length = 40)
    private String marca;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(nullable = false)
    private Integer anio;

    @Column(length = 17)
    private String vin;

    /** Marca devuelta por vPIC; se compara con la registrada (RN-04). */
    @Column(length = 40)
    private String marcaVpic;

    @Column(length = 60)
    private String motor;

    @Column(length = 40)
    private String combustible;

    /** null: todavía sin VIN. true: datos de vPIC. false: cargados a mano. */
    private Boolean datosVerificados;

    /** true si la marca de vPIC no coincide con la registrada: bloquea el presupuesto (RN-04). */
    private boolean marcaInconsistente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public void recalcularConsistencia() {
        this.marcaInconsistente = marcaVpic != null && !marcaVpic.equalsIgnoreCase(marca);
    }
}
