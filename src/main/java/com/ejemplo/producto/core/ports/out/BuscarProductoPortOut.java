package com.ejemplo.producto.core.ports.out;

import com.ejemplo.producto.core.model.Producto;

import java.util.List;
import java.util.Optional;

/** Lo que el núcleo necesita para consultar productos. */
public interface BuscarProductoPortOut {

    Optional<Producto> buscarPorId(Long id);

    List<Producto> buscarTodos();

    boolean existePorId(Long id);
}
