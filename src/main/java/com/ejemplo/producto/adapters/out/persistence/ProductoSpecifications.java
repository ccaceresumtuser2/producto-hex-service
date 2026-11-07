package com.ejemplo.producto.adapters.out.persistence;

import com.ejemplo.producto.core.model.FiltroProducto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Traduce un {@link FiltroProducto} del dominio a una consulta JPA (Criteria API).
 * Solo agrega condiciones para los criterios presentes, así un filtro vacío devuelve todo.
 */
final class ProductoSpecifications {

    private static final char ESCAPE = '\\';

    private ProductoSpecifications() {
    }

    static Specification<ProductoEntity> desde(FiltroProducto filtro) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (filtro.nombre() != null) {
                // LOWER(nombre) LIKE %texto%  -> búsqueda parcial sin distinguir mayúsculas
                String patron = "%" + escaparLike(filtro.nombre().toLowerCase(Locale.ROOT)) + "%";
                condiciones.add(cb.like(cb.lower(root.get("nombre")), patron, ESCAPE));
            }
            if (filtro.categoria() != null) {
                condiciones.add(cb.equal(root.get("categoria"), filtro.categoria()));
            }
            if (filtro.activo() != null) {
                condiciones.add(cb.equal(root.get("activo"), filtro.activo()));
            }
            if (filtro.precioMin() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("precio"), filtro.precioMin()));
            }
            if (filtro.precioMax() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("precio"), filtro.precioMax()));
            }

            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    /** Evita que % y _ escritos por el usuario actúen como comodines de LIKE. */
    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
