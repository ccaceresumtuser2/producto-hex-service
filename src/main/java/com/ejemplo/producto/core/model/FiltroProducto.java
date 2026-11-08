package com.ejemplo.producto.core.model;

import java.math.BigDecimal;

/**
 * Criterios para buscar productos. Todos son opcionales: un criterio null no filtra.
 * Los criterios presentes se combinan con Y (AND).
 *
 * @param nombre    texto que debe contener el nombre (sin distinguir mayúsculas)
 * @param categoria categoría exacta
 * @param activo    solo activos (true) o solo inactivos (false)
 * @param precioMin precio mínimo, inclusive
 * @param precioMax precio máximo, inclusive
 */
public record FiltroProducto(String nombre, Categoria categoria, Boolean activo,
                             BigDecimal precioMin, BigDecimal precioMax) {

    /** Valida los criterios y normaliza el nombre (vacío se trata igual que ausente). */
    public FiltroProducto {
        nombre = (nombre == null || nombre.isBlank()) ? null : nombre.trim();
        if (precioMin != null && precioMin.signum() < 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser negativo");
        }
        if (precioMax != null && precioMax.signum() < 0) {
            throw new IllegalArgumentException("El precio máximo no puede ser negativo");
        }
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo");
        }
    }

    /** Filtro sin criterios: equivale a listar todos los productos. */
    public static FiltroProducto vacio() {
        return new FiltroProducto(null, null, null, null, null);
    }

    /** true si no hay ningún criterio. */
    public boolean estaVacio() {
        return nombre == null && categoria == null && activo == null && precioMin == null && precioMax == null;
    }
}
