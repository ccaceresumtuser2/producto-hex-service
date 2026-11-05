package com.ejemplo.producto.core.usecase;

import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import com.ejemplo.producto.core.ports.in.EliminarProductoPortIn;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;
import com.ejemplo.producto.core.ports.out.EliminarProductoPortOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Verifica que el producto exista y lo elimina. */
public class EliminarProducto implements EliminarProductoPortIn {

    private static final Logger log = LoggerFactory.getLogger(EliminarProducto.class);

    private final BuscarProductoPortOut buscarProducto;
    private final EliminarProductoPortOut eliminarProducto;

    public EliminarProducto(BuscarProductoPortOut buscarProducto,
                            EliminarProductoPortOut eliminarProducto) {
        this.buscarProducto = buscarProducto;
        this.eliminarProducto = eliminarProducto;
    }

    @Override
    public void eliminar(Long id) {
        if (!buscarProducto.existePorId(id)) {
            log.info("No se puede eliminar: no existe el producto id={}", id);
            throw new ProductoNoEncontradoException(id);
        }
        eliminarProducto.eliminarPorId(id);
        log.info("Producto eliminado id={}", id);
    }
}
