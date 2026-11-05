package com.ejemplo.producto.core.exception;

/**
 * El precio no cumple las reglas de negocio.
 * Es unchecked (RuntimeException) para no obligar a declararla; GlobalExceptionHandler la convierte en 400.
 */
public class PrecioInvalidoException extends RuntimeException {

    public PrecioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
