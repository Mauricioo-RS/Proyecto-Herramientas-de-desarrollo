package com.proyecto.minimarket.model;

import com.proyecto.minimarket.productos.Producto;
import jakarta.persistence.*;

@Entity
@Table(name = "detalle_ventas")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    private int cantidad;
    private Double precioUnitario;
    private Double subtotal;

    public DetalleVenta() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    public Double getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getSubtotal() {
        return subtotal;
    }
}
