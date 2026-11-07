package com.ejemplo.producto.adapters.out.persistence;

import com.ejemplo.producto.core.model.FiltroProducto;
import com.ejemplo.producto.core.model.Producto;
import com.ejemplo.producto.core.ports.out.BuscarProductoPortOut;
import com.ejemplo.producto.core.ports.out.EliminarProductoPortOut;
import com.ejemplo.producto.core.ports.out.GuardarProductoPortOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementa los tres puertos de salida usando Spring Data JPA.
 * El núcleo los ve como interfaces separadas; aquí basta una sola clase porque todos usan la misma tabla.
 */
@Component
public class ProductoRepositoryAdapter
        implements BuscarProductoPortOut, GuardarProductoPortOut, EliminarProductoPortOut {

    private static final Logger log = LoggerFactory.getLogger(ProductoRepositoryAdapter.class);

    private final ProductoJpaRepository jpaRepository;
    private final ProductoMapper mapper;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpaRepository, ProductoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        log.debug("BD: findById({})", id);
        return jpaRepository.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Producto> buscarTodos() {
        log.debug("BD: findAll()");
        return jpaRepository.findAll().stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Producto> buscarPorFiltro(FiltroProducto filtro) {
        log.debug("BD: findAll(Specification) con {}", filtro);
        return jpaRepository.findAll(ProductoSpecifications.desde(filtro)).stream()
                .map(mapper::aDominio).toList();
    }

    // saveAndFlush (y no save) envía el INSERT/UPDATE de inmediato: así una violación del índice único
    // se lanza aquí como DataIntegrityViolationException y GlobalExceptionHandler responde 409.
    @Override
    public Producto guardar(Producto producto) {
        log.debug("BD: saveAndFlush(id={}, codigo={})", producto.getId(), producto.getCodigo());
        ProductoEntity guardada = jpaRepository.saveAndFlush(mapper.aEntidad(producto));
        return mapper.aDominio(guardada);
    }

    @Override
    public boolean existePorId(Long id) {
        boolean existe = jpaRepository.existsById(id);
        log.debug("BD: existsById({}) = {}", id, existe);
        return existe;
    }

    @Override
    public void eliminarPorId(Long id) {
        log.debug("BD: deleteById({})", id);
        jpaRepository.deleteById(id);
    }
}
