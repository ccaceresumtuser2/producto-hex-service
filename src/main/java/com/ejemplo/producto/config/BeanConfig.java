package com.ejemplo.producto.config;

import com.ejemplo.producto.core.ports.in.CambiarPrecioProductoPortIn;
import com.ejemplo.producto.core.ports.in.CrearProductoPortIn;
import com.ejemplo.producto.core.ports.in.EliminarProductoPortIn;
import com.ejemplo.producto.core.ports.in.ObtenerProductoPortIn;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;
import com.ejemplo.producto.core.ports.out.EliminarProductoPortOut;
import com.ejemplo.producto.core.ports.out.GuardarProductoPortOut;
import com.ejemplo.producto.core.usecase.CambiarPrecioProducto;
import com.ejemplo.producto.core.usecase.CrearProducto;
import com.ejemplo.producto.core.usecase.EliminarProducto;
import com.ejemplo.producto.core.usecase.ObtenerProducto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans de Spring.
 * Así el núcleo (core) no necesita anotaciones de Spring.
 */
@Configuration
public class BeanConfig {

    // Cada método devuelve un puerto de entrada (interfaz) y recibe los puertos de salida que necesita.
    // Spring los encuentra en ProductoRepositoryAdapter, que implementa los tres.

    @Bean
    public CrearProductoPortIn crearProductoPortIn(GuardarProductoPortOut guardar) {
        return new CrearProducto(guardar);
    }

    @Bean
    public ObtenerProductoPortIn obtenerProductoPortIn(BuscarProductoPortOut buscar) {
        return new ObtenerProducto(buscar);
    }

    @Bean
    public CambiarPrecioProductoPortIn cambiarPrecioProductoPortIn(BuscarProductoPortOut buscar,
                                                                   GuardarProductoPortOut guardar) {
        return new CambiarPrecioProducto(buscar, guardar);
    }

    @Bean
    public EliminarProductoPortIn eliminarProductoPortIn(BuscarProductoPortOut buscar,
                                                         EliminarProductoPortOut eliminar) {
        return new EliminarProducto(buscar, eliminar);
    }
}
