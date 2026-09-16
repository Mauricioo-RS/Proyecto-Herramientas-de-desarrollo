package com.proyecto.minimarket.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.minimarket.model.Venta;
import com.proyecto.minimarket.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<Venta>> listarVentas() {
        List<Venta> ventas = ventaService.listarVentas();
        return new ResponseEntity<>(ventas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> buscarVentaPorId(@PathVariable Long id) {
        Optional<Venta> venta = ventaService.buscarPorId(id);
        return venta.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Venta> guardarVenta(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.guardarVenta(venta);
        return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        if (ventaService.buscarPorId(id).isPresent()) {
            ventaService.eliminarVenta(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}