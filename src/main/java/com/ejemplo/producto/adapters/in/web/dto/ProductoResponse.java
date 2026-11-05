package com.ejemplo.producto.adapters.in.web.dto;

import com.ejemplo.producto.core.model.Categoria;
import com.ejemplo.producto.core.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos devueltos al cliente.
 * Separar este DTO del modelo permite cambiar el dominio sin romper el JSON que reciben los clientes.
 */
public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        int stock,
        Categoria categoria,
        boolean activo,
        LocalDateTime fechaCreacion
) {

    /** Convierte un producto del dominio al formato de la API. */
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getCategoria(), p.isActivo(), p.getFechaCreacion());
    }
}
