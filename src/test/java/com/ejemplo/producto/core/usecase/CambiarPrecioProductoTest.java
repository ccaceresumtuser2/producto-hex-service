package com.ejemplo.producto.core.usecase;

import com.ejemplo.producto.core.exception.PrecioInvalidoException;
import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import com.ejemplo.producto.core.model.Categoria;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;
import com.ejemplo.producto.core.ports.out.GuardarProductoPortOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CambiarPrecioProductoTest {

    @Mock
    private BuscarProductoPortOut buscarProducto;

    @Mock
    private GuardarProductoPortOut guardarProducto;

    private CambiarPrecioProducto casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new CambiarPrecioProducto(buscarProducto, guardarProducto);
    }

    private Producto productoExistente() {
        return Producto.reconstruir(1L, "HOG-0001", "Licuadora", null, new BigDecimal("250000.00"),
                25, Categoria.HOGAR, true, LocalDateTime.now());
    }

    @Test
    void cambiaElPrecioYGuarda() {
        when(buscarProducto.buscarPorId(1L)).thenReturn(Optional.of(productoExistente()));
        when(guardarProducto.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = casoDeUso.cambiarPrecio(1L, new BigDecimal("199900"));

        assertThat(resultado.getPrecio()).isEqualByComparingTo("199900.00");
        verify(guardarProducto).guardar(resultado);
    }

    @Test
    void lanzaErrorSiElProductoNoExiste() {
        when(buscarProducto.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.cambiarPrecio(99L, BigDecimal.TEN))
                .isInstanceOf(ProductoNoEncontradoException.class);
        verify(guardarProducto, never()).guardar(any());
    }

    @Test
    void noGuardaSiElPrecioEsInvalido() {
        when(buscarProducto.buscarPorId(1L)).thenReturn(Optional.of(productoExistente()));

        assertThatThrownBy(() -> casoDeUso.cambiarPrecio(1L, BigDecimal.ZERO))
                .isInstanceOf(PrecioInvalidoException.class);
        verify(guardarProducto, never()).guardar(any());
    }
}
