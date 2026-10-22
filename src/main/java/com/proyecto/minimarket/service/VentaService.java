package com.proyecto.minimarket.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.EntityManager;

import com.proyecto.minimarket.model.DetalleVenta;
import com.proyecto.minimarket.model.Venta;
import com.proyecto.minimarket.productos.Producto;
import com.proyecto.minimarket.productos.ProductoRepository;
import com.proyecto.minimarket.repository.VentaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleVentaService detalleVentaService;
    private final EntityManager entityManager;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        DetalleVentaService detalleVentaService, EntityManager entityManager) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.detalleVentaService = detalleVentaService;
        this.entityManager = entityManager;
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> buscarPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public Venta guardarVenta(Venta venta) {
        if (venta == null || venta.getId() != null || venta.getDetalles() == null
                || venta.getDetalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Una venta nueva debe tener detalles y no debe incluir un ID de venta.");
        }
        venta.getDetalles().forEach(detalleVentaService::validarDetalleNuevo);
        Map<Long, Producto> productos = bloquearProductos(venta.getDetalles());
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productos.get(detalle.getProducto().getId());
            detalleVentaService.completarDetalle(detalle, producto);
            if (detalle.getCantidad() > producto.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre()
                                + ". Disponible: " + producto.getStock() + ".");
            }
            producto.setStock(producto.getStock() - detalle.getCantidad());
            total = total.add(detalle.getSubtotal());
        }
        if (venta.getFecha() == null) {
            venta.setFecha(LocalDateTime.now());
        }
        venta.setTotal(total);
        venta.registrarDescuentoStock();
        // JPA confirma los cambios de stock junto con la venta, en la misma transacción.
        return ventaRepository.save(venta);
    }

    @Transactional
    public void eliminarVenta(Long id) {
        Venta venta = ventaRepository.buscarParaEliminar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada."));
        if (venta.isStockDescontado()) {
            Map<Long, Producto> productos = bloquearProductos(venta.getDetalles());
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = productos.get(detalle.getProducto().getId());
                long stockRestituido = (long) producto.getStock() + detalle.getCantidad();
                if (stockRestituido > Integer.MAX_VALUE) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "El stock resultante supera el máximo permitido.");
                }
                producto.setStock((int) stockRestituido);
            }
        }
        ventaRepository.delete(venta);
    }

    private Map<Long, Producto> bloquearProductos(List<DetalleVenta> detalles) {
        Map<Long, Producto> productos = new HashMap<>();
        // Un orden fijo evita bloquear los mismos productos en orden inverso.
        detalles.stream().map(detalle -> detalle.getProducto().getId()).distinct().sorted().forEach(id -> {
            Producto producto = productoRepository.buscarParaActualizarStock(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No existe el producto con ID " + id + "."));
            // Al eliminar, la relación de la venta pudo cargar el producto antes de tomar el bloqueo.
            entityManager.refresh(producto);
            productos.put(id, producto);
        });
        return productos;
    }
}
