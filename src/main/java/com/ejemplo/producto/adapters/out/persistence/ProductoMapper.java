package com.ejemplo.producto.adapters.out.persistence;

import com.ejemplo.producto.core.model.Producto;
import org.springframework.stereotype.Component;

/** Convierte entre el modelo del dominio (Producto) y la entidad JPA (ProductoEntity). */
@Component
public class ProductoMapper {

    /** Dominio -> entidad, antes de guardar. */
    public ProductoEntity aEntidad(Producto p) {
        return new ProductoEntity(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(),
                p.getPrecio(), p.getStock(), p.getCategoria(), p.isActivo(), p.getFechaCreacion());
    }

    /** Entidad -> dominio, después de leer. Usa reconstruir, que vuelve a validar los datos. */
    public Producto aDominio(ProductoEntity e) {
        return Producto.reconstruir(e.getId(), e.getCodigo(), e.getNombre(), e.getDescripcion(),
                e.getPrecio(), e.getStock(), e.getCategoria(), e.getActivo(), e.getFechaCreacion());
    }
}
