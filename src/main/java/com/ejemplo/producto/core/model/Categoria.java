package com.ejemplo.producto.core.model;

/**
 * Categorías permitidas para un producto.
 * Se guarda en la base de datos como texto (el nombre de la constante), por eso no conviene renombrarlas.
 * Al agregar una constante, actualizar también el CHECK ck_productos_categoria de ProductoEntity y de la base de datos.
 */
public enum Categoria {
    TECNOLOGIA,
    HOGAR,
    ROPA,
    ALIMENTOS,
    BEBIDAS,
    COMIDA,
    OTROS
}
