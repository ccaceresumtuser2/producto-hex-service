package com.ejemplo.producto.core.usecase;

import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.in.CambiarPrecioProductoPortIn;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;
import com.ejemplo.producto.core.ports.out.GuardarProductoPortOut;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Busca el producto, cambia el precio y lo guarda. */
public class CambiarPrecioProducto implements CambiarPrecioProductoPortIn {

    private static final Logger log = LoggerFactory.getLogger(CambiarPrecioProducto.class);

    private final BuscarProductoPortOut buscarProducto;
    private final GuardarProductoPortOut guardarProducto;

    public CambiarPrecioProducto(BuscarProductoPortOut buscarProducto,
                                 GuardarProductoPortOut guardarProducto) {
        this.buscarProducto = buscarProducto;
        this.guardarProducto = guardarProducto;
    }

    @Override
    public Producto cambiarPrecio(Long id, BigDecimal nuevoPrecio) {
        // 1. Buscar el producto (o fallar con 404).
        Producto producto = buscarProducto.buscarPorId(id)
                .orElseThrow(() -> {
                    log.info("No se puede cambiar el precio: no existe el producto id={}", id);
                    return new ProductoNoEncontradoException(id);
                });
        // 2. El dominio aplica la regla de negocio del precio.
        BigDecimal precioAnterior = producto.getPrecio();
        producto.cambiarPrecio(nuevoPrecio);
        // 3. Guardar el cambio.
        Producto guardado = guardarProducto.guardar(producto);
        log.info("Precio del producto id={} cambiado de {} a {}", id, precioAnterior, guardado.getPrecio());
        return guardado;
    }
}
