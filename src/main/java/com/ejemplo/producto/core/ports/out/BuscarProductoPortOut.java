package com.ejemplo.producto.core.ports.out;

import com.ejemplo.producto.core.model.FiltroProducto;
import com.ejemplo.producto.core.model.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el núcleo necesita para consultar productos.
 * El núcleo define la interfaz; la implementa un adaptador de salida (ProductoRepositoryAdapter).
 */
public interface BuscarProductoPortOut {

    /** Optional vacío si no existe: qué hacer en ese caso lo decide el caso de uso. */
    Optional<Producto> buscarPorId(Long id);

    List<Producto> buscarTodos();

    /** Productos que cumplen todos los criterios presentes en el filtro. */
    List<Producto> buscarPorFiltro(FiltroProducto filtro);

    /** Más barato que buscarPorId cuando solo interesa saber si existe. */
    boolean existePorId(Long id);
}
