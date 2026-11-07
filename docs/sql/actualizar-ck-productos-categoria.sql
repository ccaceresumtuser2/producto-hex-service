-- Actualiza la restricción de categorías en una tabla productos que ya existe.
-- Hibernate (ddl-auto: update) crea los CHECK solo al crear la tabla; no los modifica después.
-- Ejecutar una vez en cada base existente (local, Render, etc.) cuando cambie el enum Categoria.
-- Debe coincidir con @Check(name = "ck_productos_categoria") de ProductoEntity.

BEGIN;

ALTER TABLE productos DROP CONSTRAINT IF EXISTS ck_productos_categoria;

ALTER TABLE productos ADD CONSTRAINT ck_productos_categoria
    CHECK (categoria IN ('TECNOLOGIA', 'HOGAR', 'ROPA', 'ALIMENTOS', 'BEBIDAS', 'COMIDA', 'OTROS'));

COMMIT;
