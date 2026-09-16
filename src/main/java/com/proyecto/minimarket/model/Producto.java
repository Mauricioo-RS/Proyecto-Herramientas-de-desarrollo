package com.proyecto.minimarket.model;

public class Producto {
    private Long id;
    private String nombre;

    public Producto() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
