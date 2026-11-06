package utnfc.isi.back.taller.vehiculos.clients;

/** Datos técnicos que se usan de la respuesta de vPIC. */
public record DatosVin(String marca, String modelo, Integer anio, String motor, String combustible) {
}
