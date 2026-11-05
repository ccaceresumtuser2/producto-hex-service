package com.ejemplo.producto.core.ports.in;

import com.ejemplo.producto.core.model.Producto;

import java.util.List;

/** Contrato para consultar productos. */
public interface ObtenerProductoPortIn {

    Producto obtenerPorId(Long id);

    List<Producto> listar();
}
