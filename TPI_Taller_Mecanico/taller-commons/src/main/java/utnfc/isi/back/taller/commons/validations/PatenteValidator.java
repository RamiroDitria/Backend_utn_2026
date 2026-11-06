package utnfc.isi.back.taller.commons.validations;

import java.util.regex.Pattern;

/** RN-01: patente argentina en formato AAA123 o AA123AA. */
public final class PatenteValidator {

    private static final Pattern PATRON = Pattern.compile("^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$");

    private PatenteValidator() {
    }

    public static String normalizar(String patente) {
        return patente == null ? null : patente.toUpperCase().replaceAll("[\\s-]", "");
    }

    public static boolean esValida(String patente) {
        return patente != null && PATRON.matcher(normalizar(patente)).matches();
    }
}
