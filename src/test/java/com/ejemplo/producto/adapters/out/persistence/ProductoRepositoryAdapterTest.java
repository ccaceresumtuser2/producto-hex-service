package com.ejemplo.producto.adapters.out.persistence;

import com.ejemplo.producto.core.model.Categoria;
import com.ejemplo.producto.core.model.FiltroProducto;
import com.ejemplo.producto.core.model.Producto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba el adaptador contra una base H2 en memoria (perfil "test", ver application-test.yml).
 * Hibernate crea la tabla a partir de ProductoEntity, igual que en PostgreSQL.
 * Cada prueba corre en una transacción que se revierte al final, así que no deja datos.
 */
@DataJpaTest
@ActiveProfiles("test")
// Usa la URL H2 de application-test.yml (con MODE=PostgreSQL) en vez de una H2 genérica autogenerada.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProductoRepositoryAdapter.class, ProductoMapper.class})
class ProductoRepositoryAdapterTest {

    @Autowired
    private ProductoRepositoryAdapter adapter;

    private Producto nuevo(String codigo) {
        return Producto.crear(codigo, "Producto de prueba", null,
                new BigDecimal("1234.56"), 3, Categoria.OTROS);
    }

    @Test
    void guardarAsignaIdYSePuedeBuscar() {
        Producto guardado = adapter.guardar(nuevo("ZZZ-9991"));

        assertThat(guardado.getId()).isNotNull();
        assertThat(adapter.buscarPorId(guardado.getId()))
                .hasValueSatisfying(p -> {
                    assertThat(p.getCodigo()).isEqualTo("ZZZ-9991");
                    assertThat(p.getPrecio()).isEqualByComparingTo("1234.56");
                    assertThat(p.getCategoria()).isEqualTo(Categoria.OTROS);
                });
    }

    @Test
    void guardarProductoExistenteActualizaElPrecio() {
        Producto guardado = adapter.guardar(nuevo("ZZZ-9992"));

        guardado.cambiarPrecio(new BigDecimal("999.99"));
        adapter.guardar(guardado);

        assertThat(adapter.buscarPorId(guardado.getId()))
                .hasValueSatisfying(p -> assertThat(p.getPrecio()).isEqualByComparingTo("999.99"));
    }

    @Test
    void eliminarQuitaElProducto() {
        Producto guardado = adapter.guardar(nuevo("ZZZ-9993"));

        adapter.eliminarPorId(guardado.getId());

        assertThat(adapter.existePorId(guardado.getId())).isFalse();
    }

    @Test
    void codigoDuplicadoViolaElIndiceUnico() {
        adapter.guardar(nuevo("ZZZ-9994"));

        assertThatThrownBy(() -> adapter.guardar(nuevo("ZZZ-9994")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /** Detecta un CHECK ck_productos_categoria desactualizado respecto al enum Categoria. */
    @ParameterizedTest
    @EnumSource(Categoria.class)
    void aceptaTodasLasCategorias(Categoria categoria) {
        Producto guardado = adapter.guardar(Producto.crear("ZZZ-80" + String.format("%02d", categoria.ordinal()),
                "Producto " + categoria, null, new BigDecimal("10.00"), 1, categoria));

        assertThat(adapter.buscarPorId(guardado.getId()))
                .hasValueSatisfying(p -> assertThat(p.getCategoria()).isEqualTo(categoria));
    }

    @Test
    void buscarPorFiltroCombinaLosCriterios() {
        Producto teclado = adapter.guardar(Producto.crear("ZZZ-9001", "Teclado Mecánico", null,
                new BigDecimal("150.00"), 5, Categoria.TECNOLOGIA));
        Producto tecladoCaro = adapter.guardar(Producto.crear("ZZZ-9002", "Teclado gamer", null,
                new BigDecimal("900.00"), 5, Categoria.TECNOLOGIA));
        Producto silla = adapter.guardar(Producto.crear("ZZZ-9003", "Silla", null,
                new BigDecimal("150.00"), 5, Categoria.HOGAR));

        // nombre parcial sin distinguir mayúsculas
        assertThat(adapter.buscarPorFiltro(new FiltroProducto("TECLADO", null, null, null, null)))
                .extracting(Producto::getId)
                .contains(teclado.getId(), tecladoCaro.getId())
                .doesNotContain(silla.getId());

        // categoría + rango de precio
        assertThat(adapter.buscarPorFiltro(new FiltroProducto(null, Categoria.TECNOLOGIA, true,
                new BigDecimal("100"), new BigDecimal("200"))))
                .extracting(Producto::getId)
                .contains(teclado.getId())
                .doesNotContain(tecladoCaro.getId(), silla.getId());
    }

    @Test
    void buscarPorFiltroTrataComodinesComoTexto() {
        Producto guardado = adapter.guardar(nuevo("ZZZ-9004"));

        assertThat(adapter.buscarPorFiltro(new FiltroProducto("%", null, null, null, null)))
                .extracting(Producto::getId)
                .doesNotContain(guardado.getId());
    }

    @Test
    void buscarTodosIncluyeLosGuardados() {
        Producto guardado = adapter.guardar(nuevo("ZZZ-9995"));

        assertThat(adapter.buscarTodos())
                .extracting(Producto::getId)
                .contains(guardado.getId());
    }
}
