package com.ejemplo.producto.core.exception;

/**
 * El producto solicitado no existe.
 * La lanzan los casos de uso; GlobalExceptionHandler la convierte en 404.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("No existe un producto con id " + id);
    }
}
