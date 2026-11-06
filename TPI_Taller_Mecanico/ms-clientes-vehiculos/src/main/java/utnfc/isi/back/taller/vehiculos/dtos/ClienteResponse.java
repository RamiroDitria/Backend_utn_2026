package utnfc.isi.back.taller.vehiculos.dtos;

import utnfc.isi.back.taller.vehiculos.models.Cliente;

public record ClienteResponse(Long id, String nombre, String apellido, String telefono, String email) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail());
    }
}
