package com.ejemplo.producto.core.ports.in;

import com.ejemplo.producto.core.model.FiltroProducto;
import com.ejemplo.producto.core.model.Producto;

import java.util.List;

/** Contrato para consultar productos. */
public interface ObtenerProductoPortIn {

    /**
     * Devuelve el producto con ese id.
     *
     * @throws com.ejemplo.producto.core.exception.ProductoNoEncontradoException si no existe
     */
    Producto obtenerPorId(Long id);

    /** Devuelve todos los productos (lista vacía si no hay ninguno). */
    List<Producto> listar();

    /**
     * Devuelve los productos que cumplen todos los criterios del filtro.
     * Un filtro vacío equivale a {@link #listar()}.
     */
    List<Producto> filtrar(FiltroProducto filtro);
}
