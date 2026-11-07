package com.ejemplo.producto.adapters.in.web;

import com.ejemplo.producto.core.exception.PrecioInvalidoException;
import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import com.ejemplo.producto.core.model.Categoria;
import com.ejemplo.producto.core.model.FiltroProducto;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.in.CambiarPrecioProductoPortIn;
import com.ejemplo.producto.core.ports.in.CrearProductoCommand;
import com.ejemplo.producto.core.ports.in.CrearProductoPortIn;
import com.ejemplo.producto.core.ports.in.EliminarProductoPortIn;
import com.ejemplo.producto.core.ports.in.ObtenerProductoPortIn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearProductoPortIn crearProducto;
    @MockitoBean
    private ObtenerProductoPortIn obtenerProducto;
    @MockitoBean
    private CambiarPrecioProductoPortIn cambiarPrecioProducto;
    @MockitoBean
    private EliminarProductoPortIn eliminarProducto;

    private Producto producto(BigDecimal precio) {
        return Producto.reconstruir(1L, "TEC-0001", "Teclado", "Mecánico", precio, 10,
                Categoria.TECNOLOGIA, true, LocalDateTime.of(2026, 11, 4, 10, 0));
    }

    @Test
    void crearDevuelve201ConLocation() throws Exception {
        CrearProductoCommand comandoEsperado = new CrearProductoCommand("TEC-0001", "Teclado", "Mecánico",
                new BigDecimal("1500.50"), 10, Categoria.TECNOLOGIA);
        when(crearProducto.crear(comandoEsperado)).thenReturn(producto(new BigDecimal("1500.50")));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"TEC-0001","nombre":"Teclado","descripcion":"Mecánico",
                                 "precio":1500.50,"stock":10,"categoria":"TECNOLOGIA"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/productos/1"))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Producto creado correctamente"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.precio").value(1500.50))
                .andExpect(jsonPath("$.data.activo").value(true));
    }

    @Test
    void crearConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"malo","nombre":"","precio":10,"stock":-1,"categoria":"HOGAR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Datos inválidos"))
                .andExpect(jsonPath("$.data.codigo").exists())
                .andExpect(jsonPath("$.data.nombre").exists())
                .andExpect(jsonPath("$.data.stock").exists());

        verifyNoInteractions(crearProducto);
    }

    @Test
    void crearConCategoriaInexistenteDevuelve400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"TEC-0001","nombre":"X","precio":10,"stock":1,"categoria":"JUGUETES"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data").isEmpty());

        verifyNoInteractions(crearProducto);
    }

    @Test
    void obtenerDevuelve200() throws Exception {
        when(obtenerProducto.obtenerPorId(1L)).thenReturn(producto(new BigDecimal("1500.50")));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Producto encontrado"))
                .andExpect(jsonPath("$.data.codigo").value("TEC-0001"))
                .andExpect(jsonPath("$.data.categoria").value("TECNOLOGIA"));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(obtenerProducto.obtenerPorId(99L)).thenThrow(new ProductoNoEncontradoException(99L));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("No existe un producto con id 99"));
    }

    @Test
    void listarDevuelveArreglo() throws Exception {
        when(obtenerProducto.filtrar(FiltroProducto.vacio()))
                .thenReturn(List.of(producto(new BigDecimal("1500.50"))));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Se encontraron 1 productos"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void listarConFiltrosLosPasaAlPuerto() throws Exception {
        FiltroProducto esperado = new FiltroProducto("tecl", Categoria.TECNOLOGIA, true,
                new BigDecimal("100"), new BigDecimal("2000"));
        when(obtenerProducto.filtrar(esperado)).thenReturn(List.of(producto(new BigDecimal("1500.50"))));

        mockMvc.perform(get("/api/productos")
                        .param("nombre", "tecl")
                        .param("categoria", "TECNOLOGIA")
                        .param("activo", "true")
                        .param("precioMin", "100")
                        .param("precioMax", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].codigo").value("TEC-0001"));
    }

    @Test
    void listarConRangoDePrecioInvertidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/productos").param("precioMin", "500").param("precioMax", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("El precio mínimo no puede ser mayor que el precio máximo"));

        verifyNoInteractions(obtenerProducto);
    }

    @Test
    void listarConCategoriaInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/productos").param("categoria", "JUGUETES"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El parámetro 'categoria' tiene un valor inválido: JUGUETES"));

        verifyNoInteractions(obtenerProducto);
    }

    @Test
    void cambiarPrecioDevuelve200() throws Exception {
        when(cambiarPrecioProducto.cambiarPrecio(eq(1L), any()))
                .thenReturn(producto(new BigDecimal("2000.00")));

        mockMvc.perform(patch("/api/productos/1/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precio\":2000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Precio actualizado correctamente"))
                .andExpect(jsonPath("$.data.precio").value(2000.00));
    }

    @Test
    void cambiarPrecioInvalidoDevuelve400() throws Exception {
        when(cambiarPrecioProducto.cambiarPrecio(eq(1L), any()))
                .thenThrow(new PrecioInvalidoException("El precio debe ser mayor que cero"));

        mockMvc.perform(patch("/api/productos/1/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precio\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El precio debe ser mayor que cero"));
    }

    @Test
    void eliminarDevuelve200ConMensaje() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Producto eliminado correctamente"));

        verify(eliminarProducto).eliminar(1L);
    }

    @Test
    void eliminarInexistenteDevuelve404() throws Exception {
        doThrow(new ProductoNoEncontradoException(99L)).when(eliminarProducto).eliminar(99L);

        mockMvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/productos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' tiene un valor inválido: abc"));
    }
}
