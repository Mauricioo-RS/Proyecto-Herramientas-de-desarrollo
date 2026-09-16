package com.proyecto.minimarket.controller;

import com.proyecto.minimarket.model.DetalleVenta;
import com.proyecto.minimarket.service.DetalleVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    // Registrar un nuevo detalle de venta
    @PostMapping
    public DetalleVenta registrarDetalle(@RequestBody DetalleVenta detalle) {
        return detalleVentaService.guardarDetalle(detalle);
    }

    // Consultar el listado de detalles registrados
    @GetMapping
    public List<DetalleVenta> listarDetalles() {
        return detalleVentaService.listarTodos();
    }
}