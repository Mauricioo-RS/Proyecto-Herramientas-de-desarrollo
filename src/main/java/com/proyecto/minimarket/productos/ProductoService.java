package com.proyecto.minimarket.productos;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto crear(Producto producto) {
        validarNombreDisponible(producto.getNombre(), null);
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCaseAndActivoTrue(categoria);
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el producto con id " + id));
    }

    @Transactional(readOnly = true)
    public Producto buscarPorNombre(String nombre) {
        return productoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el producto " + nombre));
    }

    public Producto actualizar(Long id, Producto datosProducto) {
        Producto producto = buscarPorId(id);
        validarNombreDisponible(datosProducto.getNombre(), id);

        producto.setNombre(datosProducto.getNombre());
        producto.setDescripcion(datosProducto.getDescripcion());
        producto.setPrecio(datosProducto.getPrecio());
        producto.setStock(datosProducto.getStock());
        producto.setCategoria(datosProducto.getCategoria());

        return productoRepository.save(producto);
    }

    public void desactivar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        productoRepository.findByNombreIgnoreCase(nombre)
                .filter(producto -> !producto.getId().equals(idActual))
                .ifPresent(producto -> {
                    throw new IllegalArgumentException("Ya existe un producto con el nombre " + nombre);
                });
    }
}