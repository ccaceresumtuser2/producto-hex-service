package com.ejemplo.producto.core.usecase;

import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.in.CrearProductoCommand;
import com.ejemplo.producto.core.ports.in.CrearProductoPortIn;
import com.ejemplo.producto.core.ports.out.GuardarProductoPortOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Crea el producto (aplicando las reglas del dominio) y lo guarda. */
public class CrearProducto implements CrearProductoPortIn {

    private static final Logger log = LoggerFactory.getLogger(CrearProducto.class);

    private final GuardarProductoPortOut guardarProducto;

    public CrearProducto(GuardarProductoPortOut guardarProducto) {
        this.guardarProducto = guardarProducto;
    }

    @Override
    public Producto crear(CrearProductoCommand comando) {
        log.debug("Creando producto codigo={}, nombre={}", comando.codigo(), comando.nombre());
        // 1. El dominio valida los datos; si algo no es válido lanza la excepción aquí mismo.
        Producto producto = Producto.crear(comando.codigo(), comando.nombre(), comando.descripcion(),
                comando.precio(), comando.stock(), comando.categoria());
        // 2. Se guarda a través del puerto de salida; la base de datos asigna el id.
        Producto guardado = guardarProducto.guardar(producto);
        log.info("Producto creado id={}, codigo={}", guardado.getId(), guardado.getCodigo());
        return guardado;
    }
}
