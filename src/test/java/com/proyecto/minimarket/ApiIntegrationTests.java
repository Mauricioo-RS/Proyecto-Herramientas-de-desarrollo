package com.proyecto.minimarket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTests {
    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    @Test
    void productosVentasYDetallesCompartenLaPersistencia() throws Exception {
        var producto = post("/api/productos", """
                {"nombre":"Producto integración","precio":5.50,"stock":10,"categoria":"Prueba"}
                """);
        assertEquals(201, producto.statusCode(), producto.body());
        var matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(producto.body());
        assertTrue(matcher.find(), producto.body());
        String productoId = matcher.group(1);

        var detalle = post("/api/detalle-ventas", """
                {"producto":{"id":%s},"cantidad":2,"precioUnitario":5.50}
                """.formatted(productoId));
        assertEquals(200, detalle.statusCode(), detalle.body());
        assertTrue(detalle.body().contains("\"subtotal\":11.0"), detalle.body());

        var venta = post("/api/ventas", """
                {"fecha":"2026-01-01T10:00:00","total":11.0,
                 "detalles":[{"producto":{"id":%s},"cantidad":2,"precioUnitario":5.50}]}
                """.formatted(productoId));
        assertEquals(201, venta.statusCode(), venta.body());

        for (String endpoint : new String[]{"/api/productos", "/api/ventas", "/api/detalle-ventas"}) {
            var response = client.send(HttpRequest.newBuilder(uri(endpoint)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode(), response.body());
            assertTrue(response.body().contains("Producto integración"), response.body());
        }
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
