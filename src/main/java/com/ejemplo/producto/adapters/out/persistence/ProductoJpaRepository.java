package com.ejemplo.producto.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Operaciones de base de datos generadas por Spring Data JPA.
 * Al extender JpaRepository se obtienen findById, findAll, save, deleteById, existsById, etc.
 * sin escribir SQL. Solo lo usa ProductoRepositoryAdapter: el núcleo no sabe que existe.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {
}
