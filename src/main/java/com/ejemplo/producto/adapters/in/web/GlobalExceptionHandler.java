package com.ejemplo.producto.adapters.in.web;

import com.ejemplo.producto.adapters.in.web.dto.RespuestaApi;
import com.ejemplo.producto.core.exception.PrecioInvalidoException;
import com.ejemplo.producto.core.exception.ProductoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones (adaptador de entrada web).
 * <p>
 * Convierte las excepciones en respuestas HTTP con el formato estándar { message, timestamp, data, status }.
 * En los errores, data es null, salvo en los de validación, donde trae el detalle por campo.
 * <p>
 * Criterio de log:
 * <ul>
 *   <li>WARN: errores del cliente (4xx). Se registra solo el mensaje, sin stack trace, porque son esperables.</li>
 *   <li>ERROR: errores del servidor (5xx). Se registra el stack trace completo para poder diagnosticarlos.</li>
 *   <li>DEBUG: detalle técnico adicional (causa original), visible solo si se activa ese nivel.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // --- Errores del dominio ---

    /** El producto solicitado no existe en el repositorio → 404. */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<RespuestaApi<Void>> productoNoEncontrado(ProductoNoEncontradoException ex) {
        log.warn("Producto no encontrado: {}", ex.getMessage());
        return RespuestaApi.error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Regla de negocio: el precio no cumple las restricciones del dominio → 400. */
    @ExceptionHandler(PrecioInvalidoException.class)
    public ResponseEntity<RespuestaApi<Void>> precioInvalido(PrecioInvalidoException ex) {
        log.warn("Precio inválido: {}", ex.getMessage());
        return RespuestaApi.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Otros datos rechazados por el dominio (validaciones en constructores, value objects, etc.) → 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespuestaApi<Void>> datosInvalidos(IllegalArgumentException ex) {
        log.warn("Datos inválidos: {}", ex.getMessage());
        return RespuestaApi.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- Errores de la petición HTTP ---

    /**
     * Falla de Bean Validation (@Valid en el DTO) → 400.
     * Devuelve en data un mapa campo → mensaje; si un campo tiene varios errores, se conserva el primero.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaApi<Map<String, String>>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        log.warn("Validación fallida en {} campo(s): {}", errores.size(), errores);
        return RespuestaApi.de(HttpStatus.BAD_REQUEST, errores, "Datos inválidos");
    }

    /** El cuerpo no se pudo deserializar: JSON mal formado, tipo incorrecto o valor de enum desconocido → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaApi<Void>> cuerpoInvalido(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de la petición ilegible");
        // La causa original (p. ej. de Jackson) puede ser larga; se deja en DEBUG.
        log.debug("Detalle del error de deserialización", ex);
        return RespuestaApi.error(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene un valor no permitido (revise categoria y tipos)");
    }

    /** Un parámetro de ruta o query no se pudo convertir al tipo esperado (p. ej. /productos/abc con id Long) → 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaApi<Void>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        log.warn("Parámetro '{}' con valor inválido: {}", ex.getName(), ex.getValue());
        return RespuestaApi.error(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue());
    }

    /** La ruta existe pero no admite el verbo HTTP usado → 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaApi<Void>> metodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        log.warn("Método {} no permitido", ex.getMethod());
        return RespuestaApi.error(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + ex.getMethod() + " no permitido en esta ruta");
    }

    /** No hay ningún controlador ni recurso estático para la ruta pedida → 404. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaApi<Void>> rutaNoEncontrada(NoResourceFoundException ex) {
        log.warn("Ruta no encontrada: /{}", ex.getResourcePath());
        return RespuestaApi.error(HttpStatus.NOT_FOUND, "La ruta /" + ex.getResourcePath() + " no existe");
    }

    // --- Errores de persistencia ---

    /**
     * La base de datos rechazó la operación por una restricción (código único, NOT NULL, FK...) → 409.
     * Al cliente se le da un mensaje genérico para no exponer detalles del esquema; el detalle queda en el log.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaApi<Void>> conflicto(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return RespuestaApi.error(HttpStatus.CONFLICT,
                "Ya existe un producto con ese código o los datos violan una restricción");
    }

    // --- Cualquier otro error ---

    /**
     * Red de seguridad para cualquier excepción no prevista → 500.
     * Se registra con stack trace completo y al cliente solo se le devuelve un mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi<Void>> errorInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return RespuestaApi.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}
