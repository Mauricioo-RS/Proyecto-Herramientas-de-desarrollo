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

    @Test
    void clientesPermiteCrearConsultarActualizarYEliminar() throws Exception {
        String datos = """
                {"nombre":"Cliente prueba","apellido":"Integracion","dni":"12345678",
                 "email":"cliente@example.com","telefono":"987654321"}
                """;
        var creado = post("/api/v1/clientes", datos);
        assertEquals(201, creado.statusCode(), creado.body());
        var matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(creado.body());
        assertTrue(matcher.find(), creado.body());
        String path = "/api/v1/clientes/" + matcher.group(1);

        var consulta = request("GET", path, null);
        assertEquals(200, consulta.statusCode(), consulta.body());
        assertTrue(consulta.body().contains("12345678"), consulta.body());

        var actualizado = request("PUT", path, datos.replace("Cliente prueba", "Cliente actualizado"));
        assertEquals(200, actualizado.statusCode(), actualizado.body());
        var lista = request("GET", "/api/v1/clientes", null);
        assertEquals(200, lista.statusCode(), lista.body());
        assertTrue(lista.body().contains("Cliente actualizado"), lista.body());

        var eliminado = request("DELETE", path, null);
        assertEquals(204, eliminado.statusCode(), eliminado.body());
        var listaFinal = request("GET", "/api/v1/clientes", null);
        assertEquals(200, listaFinal.statusCode(), listaFinal.body());
        assertFalse(listaFinal.body().contains("12345678"), listaFinal.body());
    }

    @Test
    void clientesRechazaDniInvalido() throws Exception {
        var respuesta = post("/api/v1/clientes", """
                {"nombre":"Cliente invalido","apellido":"Prueba","dni":"abc"}
                """);
        assertEquals(400, respuesta.statusCode(), respuesta.body());
        var lista = request("GET", "/api/v1/clientes", null);
        assertEquals(200, lista.statusCode(), lista.body());
        assertFalse(lista.body().contains("Cliente invalido"), lista.body());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return request("POST", path, body);
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
