package com.proyecto.minimarket;

import com.proyecto.minimarket.productos.ProductoRepository;
import com.proyecto.minimarket.repository.DetalleVentaRepository;
import com.proyecto.minimarket.repository.VentaRepository;
import com.proyecto.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.datasource.url=jdbc:h2:mem:minimarket-legacy-test",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:ventas-legacy-schema.sql"
})
class VentasLegacySchemaTests {
    @Autowired
    private VentaRepository ventas;
    @Autowired
    private ProductoRepository productos;
    @Autowired
    private DetalleVentaRepository detalles;
    @Autowired
    private VentaService servicio;

    @Test
    void actualizaElEsquemaSinDescontarNiRestituirVentasAnteriores() {
        var venta = ventas.findById(100L).orElseThrow();
        assertFalse(venta.isStockDescontado());
        assertEquals(0, new BigDecimal("10.00").compareTo(venta.getTotal()));
        var detalle = detalles.findById(100L).orElseThrow();
        assertEquals(0, new BigDecimal("5.00").compareTo(detalle.getPrecioUnitario()));
        assertEquals(0, new BigDecimal("10.00").compareTo(detalle.getSubtotal()));
        assertEquals(30, productos.findById(100L).orElseThrow().getStock());

        servicio.eliminarVenta(100L);
        assertFalse(ventas.existsById(100L));
        assertFalse(detalles.existsById(100L));
        assertEquals(30, productos.findById(100L).orElseThrow().getStock());
    }
}
