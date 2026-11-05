package com.ejemplo.producto.core.ports.out;

/** Lo que el núcleo necesita para eliminar un producto. */
public interface EliminarProductoPortOut {

    void eliminarPorId(Long id);
}
