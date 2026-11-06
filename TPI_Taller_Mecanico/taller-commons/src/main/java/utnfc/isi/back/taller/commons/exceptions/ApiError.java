package utnfc.isi.back.taller.commons.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato estándar de error que devuelven todos los servicios. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String codigo,
        String mensaje,
        Map<String, String> detalles) {

    public static ApiError of(int status, String codigo, String mensaje) {
        return new ApiError(LocalDateTime.now(), status, codigo, mensaje, null);
    }

    public static ApiError of(int status, String codigo, String mensaje, Map<String, String> detalles) {
        return new ApiError(LocalDateTime.now(), status, codigo, mensaje, detalles);
    }
}
