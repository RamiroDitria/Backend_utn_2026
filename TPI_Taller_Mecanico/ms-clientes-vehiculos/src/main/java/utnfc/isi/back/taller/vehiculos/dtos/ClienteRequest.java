package utnfc.isi.back.taller.vehiculos.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,
        @NotBlank(message = "El apellido es obligatorio") @Size(max = 60) String apellido,
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Teléfono con código de país, solo números (ej: 5493511234567)")
        String telefono,
        @Email(message = "Email inválido") String email) {
}
