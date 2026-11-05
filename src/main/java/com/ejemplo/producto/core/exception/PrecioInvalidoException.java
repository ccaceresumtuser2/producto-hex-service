package com.ejemplo.producto.core.exception;

/** El precio no cumple las reglas de negocio. */
public class PrecioInvalidoException extends RuntimeException {

    public PrecioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
