package com.proyecto.minimarket.service;

import com.proyecto.minimarket.model.DetalleVenta;
import com.proyecto.minimarket.repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleVentaService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public List<DetalleVenta> listarDetalles() {
        return detalleVentaRepository.findAll();
    }

    public Optional<DetalleVenta> buscarPorId(Long id) {
        return detalleVentaRepository.findById(id);
    }

    public DetalleVenta guardarDetalle(DetalleVenta detalle) {
        if (detalle != null) {
            // Se calcula el subtotal multiplicando cantidad por precio unitario
            double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
            detalle.setSubtotal(subtotal);
            return detalleVentaRepository.save(detalle);
        }
        return null;
    }

    public void eliminarDetalle(Long id) {
        detalleVentaRepository.deleteById(id);
    }
}