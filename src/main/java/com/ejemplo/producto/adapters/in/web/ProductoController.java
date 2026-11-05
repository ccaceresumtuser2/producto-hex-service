package com.ejemplo.producto.adapters.in.web;

import com.ejemplo.producto.adapters.in.web.dto.CambiarPrecioRequest;
import com.ejemplo.producto.adapters.in.web.dto.CrearProductoRequest;
import com.ejemplo.producto.adapters.in.web.dto.ProductoResponse;
import com.ejemplo.producto.adapters.in.web.dto.RespuestaApi;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.in.CambiarPrecioProductoPortIn;
import com.ejemplo.producto.core.ports.in.CrearProductoPortIn;
import com.ejemplo.producto.core.ports.in.EliminarProductoPortIn;
import com.ejemplo.producto.core.ports.in.ObtenerProductoPortIn;
import com.ejemplo.producto.adapters.in.web.doc.EjemplosRespuesta;
import com.ejemplo.producto.adapters.in.web.doc.RespuestaListaProductosDoc;
import com.ejemplo.producto.adapters.in.web.doc.RespuestaProductoDoc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Recibe peticiones HTTP y llama a los puertos de entrada. No conoce los casos de uso concretos.
 * Todas las respuestas usan el formato {@link RespuestaApi}: { message, timestamp, data, status }.
 */
@Tag(name = "Productos", description = "Crear, consultar, cambiar precio y eliminar productos")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);
    private static final String JSON = "application/json";

    // Depende de los puertos (interfaces), no de las clases de casos de uso: Spring inyecta los beans de BeanConfig.
    private final CrearProductoPortIn crearProducto;
    private final ObtenerProductoPortIn obtenerProducto;
    private final CambiarPrecioProductoPortIn cambiarPrecioProducto;
    private final EliminarProductoPortIn eliminarProducto;

    public ProductoController(CrearProductoPortIn crearProducto,
                              ObtenerProductoPortIn obtenerProducto,
                              CambiarPrecioProductoPortIn cambiarPrecioProducto,
                              EliminarProductoPortIn eliminarProducto) {
        this.crearProducto = crearProducto;
        this.obtenerProducto = obtenerProducto;
        this.cambiarPrecioProducto = cambiarPrecioProducto;
        this.eliminarProducto = eliminarProducto;
    }

    @Operation(summary = "Crear un producto")
    @ApiResponse(responseCode = "201", description = "Producto creado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaProductoDoc.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.CREADO)))
    @ApiResponse(responseCode = "400", description = "Datos o precio inválidos",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class), examples = {
                    @ExampleObject(name = "Validación", value = EjemplosRespuesta.VALIDACION),
                    @ExampleObject(name = "Precio inválido", value = EjemplosRespuesta.PRECIO_INVALIDO)}))
    @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese código",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.DUPLICADO)))
    @PostMapping
    public ResponseEntity<RespuestaApi<ProductoResponse>> crear(@Valid @RequestBody CrearProductoRequest request) {
        log.info("POST /api/productos - crear producto codigo={}", request.codigo());
        Producto creado = crearProducto.crear(request.aComando());
        // 201 Created incluye la cabecera Location con la URL del nuevo recurso: /api/productos/{id}
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(RespuestaApi.nueva(
                HttpStatus.CREATED, ProductoResponse.desde(creado), "Producto creado correctamente"));
    }

    @Operation(summary = "Obtener un producto por id")
    @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaProductoDoc.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.ENCONTRADO)))
    @ApiResponse(responseCode = "404", description = "El producto no existe",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.NO_ENCONTRADO)))
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<ProductoResponse>> obtener(@PathVariable Long id) {
        log.info("GET /api/productos/{} - obtener producto", id);
        return RespuestaApi.ok(ProductoResponse.desde(obtenerProducto.obtenerPorId(id)),
                "Producto encontrado");
    }

    @Operation(summary = "Listar todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista de productos",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaListaProductosDoc.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.LISTA)))
    @GetMapping
    public ResponseEntity<RespuestaApi<List<ProductoResponse>>> listar() {
        log.info("GET /api/productos - listar productos");
        List<ProductoResponse> productos = obtenerProducto.listar().stream()
                .map(ProductoResponse::desde).toList();
        log.debug("Se devuelven {} productos", productos.size());
        return RespuestaApi.ok(productos, "Se encontraron " + productos.size() + " productos");
    }

    @Operation(summary = "Cambiar el precio de un producto")
    @ApiResponse(responseCode = "200", description = "Precio actualizado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaProductoDoc.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.PRECIO_ACTUALIZADO)))
    @ApiResponse(responseCode = "400", description = "Precio inválido",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.PRECIO_INVALIDO)))
    @ApiResponse(responseCode = "404", description = "El producto no existe",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.NO_ENCONTRADO)))
    @PatchMapping("/{id}/precio")
    public ResponseEntity<RespuestaApi<ProductoResponse>> cambiarPrecio(
            @PathVariable Long id, @Valid @RequestBody CambiarPrecioRequest request) {
        log.info("PATCH /api/productos/{}/precio - nuevo precio={}", id, request.precio());
        return RespuestaApi.ok(ProductoResponse.desde(cambiarPrecioProducto.cambiarPrecio(id, request.precio())),
                "Precio actualizado correctamente");
    }

    @Operation(summary = "Eliminar un producto")
    @ApiResponse(responseCode = "200", description = "Producto eliminado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.ELIMINADO)))
    @ApiResponse(responseCode = "404", description = "El producto no existe",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosRespuesta.NO_ENCONTRADO)))
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/productos/{} - eliminar producto", id);
        eliminarProducto.eliminar(id);
        // Se responde 200 con mensaje (y no 204 sin cuerpo) para mantener el formato RespuestaApi.
        return RespuestaApi.ok(null, "Producto eliminado correctamente");
    }
}
