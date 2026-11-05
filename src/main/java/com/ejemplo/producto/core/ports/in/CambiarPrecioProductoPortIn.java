package com.ejemplo.producto.core.ports.in;

import com.ejemplo.producto.core.model.Producto;

import java.math.BigDecimal;

/** Contrato para cambiar el precio de un producto. */
public interface CambiarPrecioProductoPortIn {

    Producto cambiarPrecio(Long id, BigDecimal nuevoPrecio);
}
