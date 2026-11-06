package utnfc.isi.back.taller.vehiculos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    /** Teléfono con código de país, al que se envían los WhatsApp (ej: 5493511234567). */
    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
