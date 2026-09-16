package com.proyecto.minimarket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.minimarket.model.Producto;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();

    public List<Producto> listarProductos() {
        return productos;
    }

    public Producto buscarProductoPorId(int id) {
        for (Producto producto : productos) {
            if (producto.getId() == id) {
                return producto;
            }
        }

        return null;
    }

    public Producto agregarProducto(Producto producto) {
        productos.add(producto);
        return producto;
    }

    public boolean eliminarProducto(int id) {
        return productos.removeIf(producto -> producto.getId() == id);
    }
}