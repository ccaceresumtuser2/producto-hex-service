package com.ejemplo.producto.adapters.out.persistence;

import com.ejemplo.producto.core.model.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa la tabla productos con JPA. Solo vive en el adaptador de persistencia.
 * <p>
 * Hibernate crea la tabla si no existe (ddl-auto: update). Por eso aquí se declaran también
 * el índice único, los índices de búsqueda y las restricciones CHECK que antes estaban en el script SQL.
 */
@Entity
@Table(name = "productos",
        uniqueConstraints = @UniqueConstraint(name = "uk_productos_codigo", columnNames = "codigo"),
        indexes = {
                // Búsquedas por nombre
                @Index(name = "idx_productos_nombre", columnList = "nombre"),
                // Filtro por categoría
                @Index(name = "idx_productos_categoria", columnList = "categoria"),
                // Filtro combinado: productos activos de una categoría
                @Index(name = "idx_productos_categoria_activo", columnList = "categoria, activo")
        })
@Check(name = "ck_productos_codigo", constraints = "codigo ~ '^[A-Z]{3}-[0-9]{4}$'")
@Check(name = "ck_productos_precio", constraints = "precio > 0")
@Check(name = "ck_productos_stock", constraints = "stock >= 0")
@Check(name = "ck_productos_categoria",
        constraints = "categoria IN ('TECNOLOGIA', 'HOGAR', 'ROPA', 'ALIMENTOS', 'OTROS')")
public class ProductoEntity {

    // IDENTITY: el id lo genera la base de datos al insertar.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 8)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    // STRING guarda el nombre (TECNOLOGIA) en lugar de la posición (0, 1, ...), que cambiaría al reordenar el enum.
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private Categoria categoria;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    // updatable = false: Hibernate nunca la incluye en un UPDATE.
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    protected ProductoEntity() {
        // requerido por JPA
    }

    /** Usado por ProductoMapper. id null significa producto nuevo (INSERT). */
    public ProductoEntity(Long id, String codigo, String nombre, String descripcion, BigDecimal precio,
                          Integer stock, Categoria categoria, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public Categoria getCategoria() { return categoria; }
    public Boolean getActivo() { return activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
