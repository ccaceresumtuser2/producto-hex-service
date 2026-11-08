package com.ejemplo.producto.core.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiltroProductoTest {

    @Test
    void nombreEnBlancoSeTrataComoAusente() {
        FiltroProducto filtro = new FiltroProducto("   ", null, null, null, null);

        assertThat(filtro.nombre()).isNull();
        assertThat(filtro.estaVacio()).isTrue();
    }

    @Test
    void nombreSeRecortaYCuentaComoCriterio() {
        FiltroProducto filtro = new FiltroProducto("  teclado ", null, null, null, null);

        assertThat(filtro.nombre()).isEqualTo("teclado");
        assertThat(filtro.estaVacio()).isFalse();
    }

    @Test
    void precioMinMayorQueMaxEsInvalido() {
        assertThatThrownBy(() -> new FiltroProducto(null, null, null,
                new BigDecimal("10"), new BigDecimal("5")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio mínimo no puede ser mayor que el precio máximo");
    }

    @Test
    void precioNegativoEsInvalido() {
        assertThatThrownBy(() -> new FiltroProducto(null, null, null, new BigDecimal("-1"), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FiltroProducto(null, null, null, null, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rangoIgualEsValido() {
        FiltroProducto filtro = new FiltroProducto(null, null, null,
                new BigDecimal("10"), new BigDecimal("10"));

        assertThat(filtro.estaVacio()).isFalse();
    }
}
