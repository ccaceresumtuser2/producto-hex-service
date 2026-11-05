package com.ejemplo.producto.adapters.in.web.dto;

import com.ejemplo.producto.core.model.Categoria;
import com.ejemplo.producto.core.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos devueltos al cliente. */
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

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getCategoria(), p.isActivo(), p.getFechaCreacion());
    }
}
