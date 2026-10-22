package com.proyecto.minimarket.service;

import com.proyecto.minimarket.model.DetalleVenta;
import com.proyecto.minimarket.productos.Producto;
import com.proyecto.minimarket.productos.ProductoRepository;
import com.proyecto.minimarket.repository.DetalleVentaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class DetalleVentaService {
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;

    public DetalleVentaService(DetalleVentaRepository detalleVentaRepository,
                              ProductoRepository productoRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
    }

    public List<DetalleVenta> listarDetalles() {
        return detalleVentaRepository.findAll();
    }

    public Optional<DetalleVenta> buscarPorId(Long id) {
        return detalleVentaRepository.findById(id);
    }

    @Transactional
    public DetalleVenta guardarDetalle(DetalleVenta detalle) {
        validarDetalleNuevo(detalle);
        Producto producto = productoRepository.findById(detalle.getProducto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
        completarDetalle(detalle, producto);
        // Un detalle independiente no es una venta y no modifica el inventario.
        return detalleVentaRepository.save(detalle);
    }

    void validarDetalleNuevo(DetalleVenta detalle) {
        if (detalle == null || detalle.getId() != null || detalle.getProducto() == null
                || detalle.getProducto().getId() == null || detalle.getProducto().getId() <= 0
                || detalle.getCantidad() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cada detalle debe indicar producto.id y una cantidad positiva, sin ID de detalle.");
        }
    }

    void completarDetalle(DetalleVenta detalle, Producto producto) {
        if (!producto.isActivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El producto " + producto.getNombre() + " está desactivado.");
        }
        detalle.setProducto(producto);
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(detalle.getCantidad())));
    }

    public void eliminarDetalle(Long id) {
        detalleVentaRepository.deleteById(id);
    }
}
