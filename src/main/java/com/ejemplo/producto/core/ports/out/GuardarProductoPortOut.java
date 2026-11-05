package com.ejemplo.producto.core.ports.out;

import com.ejemplo.producto.core.model.Producto;

/** Lo que el núcleo necesita para guardar (crear o actualizar) un producto. */
public interface GuardarProductoPortOut {

    /**
     * Si el producto no tiene id, lo inserta; si ya lo tiene, lo actualiza.
     *
     * @return el producto tal como quedó guardado (con el id asignado)
     */
    Producto guardar(Producto producto);
}
