package com.ejemplo.producto.core.ports.in;

import com.ejemplo.producto.core.model.Producto;

/** Contrato para crear un producto. */
public interface CrearProductoPortIn {

    /**
     * Crea un producto nuevo aplicando las reglas del dominio y lo guarda.
     *
     * @return el producto guardado, ya con id asignado
     * @throws IllegalArgumentException si algún dato no es válido
     * @throws com.ejemplo.producto.core.exception.PrecioInvalidoException si el precio no es válido
     */
    Producto crear(CrearProductoCommand comando);
}
