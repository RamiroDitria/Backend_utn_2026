package utnfc.isi.back.taller.vehiculos.dtos;

import utnfc.isi.back.taller.vehiculos.models.Vehiculo;

public record VehiculoResponse(
        Long id,
        String patente,
        String marca,
        String modelo,
        Integer anio,
        String vin,
        String marcaVpic,
        String motor,
        String combustible,
        Boolean datosVerificados,
        boolean marcaInconsistente,
        Long clienteId,
        String clienteNombre,
        String clienteTelefono) {

    public static VehiculoResponse from(Vehiculo v) {
        return new VehiculoResponse(
                v.getId(), v.getPatente(), v.getMarca(), v.getModelo(), v.getAnio(), v.getVin(),
                v.getMarcaVpic(), v.getMotor(), v.getCombustible(), v.getDatosVerificados(),
                v.isMarcaInconsistente(), v.getCliente().getId(), v.getCliente().getNombreCompleto(),
                v.getCliente().getTelefono());
    }
}
