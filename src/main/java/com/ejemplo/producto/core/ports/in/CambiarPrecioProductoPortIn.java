package com.ejemplo.producto.core.ports.in;

import com.ejemplo.producto.core.model.Producto;

import java.math.BigDecimal;

/** Contrato para cambiar el precio de un producto. */
public interface CambiarPrecioProductoPortIn {

    /**
     * Cambia el precio del producto y lo guarda.
     *
     * @return el producto con el precio actualizado
     * @throws com.ejemplo.producto.core.exception.ProductoNoEncontradoException si no existe
     * @throws com.ejemplo.producto.core.exception.PrecioInvalidoException si el precio no es válido
     */
    Producto cambiarPrecio(Long id, BigDecimal nuevoPrecio);
}
