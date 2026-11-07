package com.ejemplo.producto.core.usecase;

import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import com.ejemplo.producto.core.model.Categoria;
import com.ejemplo.producto.core.model.FiltroProducto;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerProductoTest {

    @Mock
    private BuscarProductoPortOut buscarProducto;

    private ObtenerProducto casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new ObtenerProducto(buscarProducto);
    }

    private Producto producto() {
        return Producto.reconstruir(1L, "TEC-0001", "Teclado", null, new BigDecimal("150000.00"),
                10, Categoria.TECNOLOGIA, true, LocalDateTime.now());
    }

    @Test
    void obtenerPorIdDevuelveElProducto() {
        when(buscarProducto.buscarPorId(1L)).thenReturn(Optional.of(producto()));

        assertThat(casoDeUso.obtenerPorId(1L).getCodigo()).isEqualTo("TEC-0001");
    }

    @Test
    void obtenerPorIdLanzaErrorSiNoExiste() {
        when(buscarProducto.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casoDeUso.obtenerPorId(99L))
                .isInstanceOf(ProductoNoEncontradoException.class);
    }

    @Test
    void filtrarConCriteriosConsultaPorFiltro() {
        FiltroProducto filtro = new FiltroProducto("tecl", Categoria.TECNOLOGIA, null, null, null);
        when(buscarProducto.buscarPorFiltro(filtro)).thenReturn(List.of(producto()));

        assertThat(casoDeUso.filtrar(filtro)).hasSize(1);
        verify(buscarProducto, never()).buscarTodos();
    }

    @Test
    void filtrarVacioListaTodos() {
        when(buscarProducto.buscarTodos()).thenReturn(List.of(producto()));

        assertThat(casoDeUso.filtrar(FiltroProducto.vacio())).hasSize(1);
        verify(buscarProducto, never()).buscarPorFiltro(any());
    }

    @Test
    void filtrarNullListaTodos() {
        when(buscarProducto.buscarTodos()).thenReturn(List.of());

        assertThat(casoDeUso.filtrar(null)).isEmpty();
        verify(buscarProducto, never()).buscarPorFiltro(any());
    }
}
