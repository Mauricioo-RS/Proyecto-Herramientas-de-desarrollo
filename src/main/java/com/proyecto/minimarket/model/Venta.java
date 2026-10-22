package com.proyecto.minimarket.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venta_id")
    private List<DetalleVenta> detalles;
    @Column(precision = 19, scale = 2)
    private BigDecimal total;

    // Las ventas anteriores a esta lógica no descontaron existencias.
    @JsonIgnore
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean stockDescontado;

    public Venta() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @JsonIgnore
    public boolean isStockDescontado() {
        return stockDescontado;
    }

    public void registrarDescuentoStock() {
        this.stockDescontado = true;
    }
}
