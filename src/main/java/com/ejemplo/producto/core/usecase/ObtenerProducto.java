package com.ejemplo.producto.core.usecase;

import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.in.ObtenerProductoPortIn;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Busca el producto o lanza un error si no existe. */
public class ObtenerProducto implements ObtenerProductoPortIn {

    private static final Logger log = LoggerFactory.getLogger(ObtenerProducto.class);

    private final BuscarProductoPortOut buscarProducto;

    public ObtenerProducto(BuscarProductoPortOut buscarProducto) {
        this.buscarProducto = buscarProducto;
    }

    @Override
    public Producto obtenerPorId(Long id) {
        log.debug("Buscando producto id={}", id);
        return buscarProducto.buscarPorId(id)
                .orElseThrow(() -> {
                    log.info("No existe el producto id={}", id);
                    return new ProductoNoEncontradoException(id);
                });
    }

    @Override
    public List<Producto> listar() {
        List<Producto> productos = buscarProducto.buscarTodos();
        log.debug("Listado de productos: {} encontrados", productos.size());
        return productos;
    }
}
