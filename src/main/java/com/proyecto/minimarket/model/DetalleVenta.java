package com.proyecto.minimarket.model;

import com.proyecto.minimarket.productos.Producto;
import java.math.BigDecimal;
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
    @Column(precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal;

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
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
