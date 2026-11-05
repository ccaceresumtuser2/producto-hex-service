package com.ejemplo.producto.core.ports.out;

import com.ejemplo.producto.core.model.Producto;

/** Lo que el núcleo necesita para guardar (crear o actualizar) un producto. */
public interface GuardarProductoPortOut {

    Producto guardar(Producto producto);
}
