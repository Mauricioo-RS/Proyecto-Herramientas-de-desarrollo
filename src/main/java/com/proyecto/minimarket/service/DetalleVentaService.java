package com.proyecto.minimarket.service;

import com.proyecto.minimarket.model.DetalleVenta;
import com.proyecto.minimarket.repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleVentaService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public DetalleVenta guardarDetalle(DetalleVenta detalle) {
        if (detalle != null) {
            // Se calcula el subtotal multiplicando cantidad por precio unitario
            double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
            detalle.setSubtotal(subtotal);
            return detalleVentaRepository.save(detalle);
        }
        return null;
    }

    public List<DetalleVenta> listarTodos() {
        return detalleVentaRepository.findAll();
    }
}