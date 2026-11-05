package com.ejemplo.producto.core.ports.in;

/** Contrato para eliminar un producto. */
public interface EliminarProductoPortIn {

    /**
     * Elimina el producto.
     *
     * @throws com.ejemplo.producto.core.exception.ProductoNoEncontradoException si no existe
     */
    void eliminar(Long id);
}
