package utnfc.isi.back.taller.commons.validations;

import java.util.regex.Pattern;

/** RN-04: el VIN tiene 17 caracteres alfanuméricos y no usa I, O ni Q. */
public final class VinValidator {

    private static final Pattern PATRON = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private VinValidator() {
    }

    public static String normalizar(String vin) {
        return vin == null ? null : vin.trim().toUpperCase();
    }

    public static boolean esValido(String vin) {
        return vin != null && PATRON.matcher(normalizar(vin)).matches();
    }
}
