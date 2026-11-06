package utnfc.isi.back.taller.ordenes.dtos;

/** Lo que ms-ordenes necesita del vehículo que devuelve ms-clientes-vehiculos. */
public record VehiculoDTO(
        Long id,
        String patente,
        String marca,
        String modelo,
        String vin,
        Boolean datosVerificados,
        boolean marcaInconsistente,
        Long clienteId,
        String clienteNombre,
        String clienteTelefono) {
}
