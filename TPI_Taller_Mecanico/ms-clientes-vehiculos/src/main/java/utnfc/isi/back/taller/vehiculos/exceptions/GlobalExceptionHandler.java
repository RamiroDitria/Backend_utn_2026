package utnfc.isi.back.taller.vehiculos.exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import utnfc.isi.back.taller.commons.exceptions.BaseExceptionHandler;

/** Hereda el manejo de errores del componente común. Acá se agregan los propios del servicio. */
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {
}
