package com.ejemplo.producto.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Operaciones de base de datos generadas por Spring Data JPA.
 * Al extender JpaRepository se obtienen findById, findAll, save, deleteById, existsById, etc.
 * sin escribir SQL. JpaSpecificationExecutor agrega findAll(Specification) para consultas dinámicas (filtros).
 * Solo lo usa ProductoRepositoryAdapter: el núcleo no sabe que existe.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long>,
        JpaSpecificationExecutor<ProductoEntity> {
}
