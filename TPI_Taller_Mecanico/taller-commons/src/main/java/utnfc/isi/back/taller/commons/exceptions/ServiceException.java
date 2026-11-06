package utnfc.isi.back.taller.commons.exceptions;

/**
 * Excepción semántica de negocio. El código identifica la regla incumplida
 * (por ejemplo "RN-06") y el status es el código HTTP con el que se responde.
 */
public class ServiceException extends RuntimeException {

    private final String codigo;
    private final int status;

    public ServiceException(String codigo, String mensaje, int status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    public static ServiceException noEncontrado(String codigo, String mensaje) {
        return new ServiceException(codigo, mensaje, 404);
    }

    public static ServiceException conflicto(String codigo, String mensaje) {
        return new ServiceException(codigo, mensaje, 409);
    }

    public static ServiceException reglaNegocio(String codigo, String mensaje) {
        return new ServiceException(codigo, mensaje, 422);
    }

    public static ServiceException prohibido(String codigo, String mensaje) {
        return new ServiceException(codigo, mensaje, 403);
    }

    public String getCodigo() {
        return codigo;
    }

    public int getStatus() {
        return status;
    }
}
