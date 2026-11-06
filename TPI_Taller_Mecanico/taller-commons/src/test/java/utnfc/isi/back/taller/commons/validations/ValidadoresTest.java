package utnfc.isi.back.taller.commons.validations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidadoresTest {

    @Test
    void patentesValidasEInvalidas() {
        assertTrue(PatenteValidator.esValida("ABC123"));
        assertTrue(PatenteValidator.esValida("ab 123 cd"));
        assertFalse(PatenteValidator.esValida("AB1234"));
        assertFalse(PatenteValidator.esValida(null));
    }

    @Test
    void vinValidoEInvalido() {
        assertTrue(VinValidator.esValido("1HGCM82633A004352"));
        assertFalse(VinValidator.esValido("1HGCM82633A00435"));   // 16 caracteres
        assertFalse(VinValidator.esValido("1HGCM82633A00435O"));  // contiene O
    }
}
