package com.proyecto.minimarket.productos;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto crear(Producto producto) {
        if (producto.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un producto nuevo no debe incluir ID.");
        }
        validarNombreDisponible(producto.getNombre(), null);
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCaseAndActivoTrue(categoria);
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> productoNoEncontrado(id));
    }

    @Transactional(readOnly = true)
    public Producto buscarPorNombre(String nombre) {
        return productoRepository.findByNombreIgnoreCase(nombre)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
    }

    public Producto actualizar(Long id, Producto datosProducto) {
        Producto producto = productoRepository.buscarParaActualizarStock(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> productoNoEncontrado(id));
        validarNombreDisponible(datosProducto.getNombre(), id);

        producto.setNombre(datosProducto.getNombre());
        producto.setDescripcion(datosProducto.getDescripcion());
        producto.setPrecio(datosProducto.getPrecio());
        producto.setStock(datosProducto.getStock());
        producto.setCategoria(datosProducto.getCategoria());

        return productoRepository.save(producto);
    }

    public void desactivar(Long id) {
        Producto producto = productoRepository.buscarParaActualizarStock(id)
                .orElseThrow(() -> productoNoEncontrado(id));
        producto.setActivo(false);
    }

    private ResponseStatusException productoNoEncontrado(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el producto con ID " + id + ".");
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        productoRepository.findByNombreIgnoreCase(nombre)
                .filter(producto -> !producto.getId().equals(idActual))
                .ifPresent(producto -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese nombre.");
                });
    }
}
