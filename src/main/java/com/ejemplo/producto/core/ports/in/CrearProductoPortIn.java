package com.ejemplo.producto.core.ports.in;

import com.ejemplo.producto.core.model.Producto;

/** Contrato para crear un producto. */
public interface CrearProductoPortIn {

    Producto crear(CrearProductoCommand comando);
}
