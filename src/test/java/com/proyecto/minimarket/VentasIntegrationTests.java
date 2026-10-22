package com.proyecto.minimarket;

import com.proyecto.minimarket.productos.Producto;
import com.proyecto.minimarket.productos.ProductoRepository;
import com.proyecto.minimarket.repository.DetalleVentaRepository;
import com.proyecto.minimarket.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VentasIntegrationTests {
    @Value("${local.server.port}")
    private int port;
    @Autowired
    private ProductoRepository productos;
    @Autowired
    private VentaRepository ventas;
    @Autowired
    private DetalleVentaRepository detalles;

    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void calculaDesdeElCatalogoDescuentaStockYDevuelveProductoCompleto() throws Exception {
        Producto producto = producto("5.00", 30);
        var respuesta = request("POST", "/api/ventas", """
                {"total":0.01,"stockDescontado":false,"detalles":[
                  {"producto":{"id":%d,"nombre":"Falso","precio":0.01,"stock":999},
                   "cantidad":2,"precioUnitario":0.01,"subtotal":0.01}]}
                """.formatted(producto.getId()));
        JsonNode venta = expect(201, respuesta);
        money("10.00", venta.get("total"));
        JsonNode detalle = venta.get("detalles").get(0);
        money("5.00", detalle.get("precioUnitario"));
        money("10.00", detalle.get("subtotal"));
        assertEquals(producto.getNombre(), detalle.get("producto").get("nombre").asText());
        assertEquals(28, detalle.get("producto").get("stock").asInt());
        assertNotNull(venta.get("fecha"));
        assertFalse(venta.has("stockDescontado"));
        assertStock(producto, 28);

        JsonNode guardada = expect(200, request("GET", "/api/ventas/" + venta.get("id").asLong(), null));
        money("10.00", guardada.get("total"));
        assertEquals(producto.getNombre(), guardada.get("detalles").get(0).get("producto").get("nombre").asText());
    }

    @Test
    void sumaVariosProductosConImportesDecimalesExactos() throws Exception {
        Producto primero = producto("0.10", 10);
        Producto segundo = producto("0.20", 10);
        var venta = expect(201, request("POST", "/api/ventas", venta(linea(primero, 3), linea(segundo, 2))));
        money("0.70", venta.get("total"));
        assertStock(primero, 7);
        assertStock(segundo, 8);
    }

    @Test
    void precioDeLaVentaSeConservaCuandoCambiaElCatalogo() throws Exception {
        Producto producto = producto("5.00", 10);
        var venta = expect(201, request("POST", "/api/ventas", venta(linea(producto, 2))));
        Producto actualizado = productos.findById(producto.getId()).orElseThrow();
        actualizado.setPrecio(new BigDecimal("8.00"));
        productos.save(actualizado);
        var guardada = expect(200, request("GET", "/api/ventas/" + venta.get("id").asLong(), null));
        money("10.00", guardada.get("total"));
        money("5.00", guardada.get("detalles").get(0).get("precioUnitario"));
    }

    @Test
    void rechazaStockInsuficienteSinGuardarVentaNiDetalles() throws Exception {
        Producto producto = producto("5.00", 1);
        long ventasAntes = ventas.count();
        long detallesAntes = detalles.count();
        var error = expect(409, request("POST", "/api/ventas", venta(linea(producto, 2))));
        assertTrue(error.get("detail").asText().contains("Stock insuficiente"));
        assertStock(producto, 1);
        assertEquals(ventasAntes, ventas.count());
        assertEquals(detallesAntes, detalles.count());
    }

    @Test
    void revierteTodoSiElSegundoProductoNoAlcanza() throws Exception {
        Producto primero = producto("5.00", 10);
        Producto segundo = producto("2.00", 1);
        long ventasAntes = ventas.count();
        long detallesAntes = detalles.count();
        expect(409, request("POST", "/api/ventas", venta(linea(primero, 2), linea(segundo, 2))));
        assertStock(primero, 10);
        assertStock(segundo, 1);
        assertEquals(ventasAntes, ventas.count());
        assertEquals(detallesAntes, detalles.count());
    }

    @Test
    void consideraLaCantidadAcumuladaSiSeRepiteUnProducto() throws Exception {
        Producto producto = producto("5.00", 5);
        expect(409, request("POST", "/api/ventas", venta(linea(producto, 3), linea(producto, 3))));
        assertStock(producto, 5);
        var venta = expect(201, request("POST", "/api/ventas", venta(linea(producto, 2), linea(producto, 3))));
        money("25.00", venta.get("total"));
        assertStock(producto, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}", "{\"detalles\":[]}", "{\"detalles\":[null]}",
        "{\"detalles\":[{\"cantidad\":1}]}",
        "{\"detalles\":[{\"producto\":{},\"cantidad\":1}]}",
        "{\"detalles\":[{\"producto\":{\"id\":$ID},\"cantidad\":0}]}",
        "{\"detalles\":[{\"producto\":{\"id\":$ID},\"cantidad\":-1}]}",
        "{\"id\":1,\"detalles\":[{\"producto\":{\"id\":$ID},\"cantidad\":1}]}",
        "{\"detalles\":[{\"id\":1,\"producto\":{\"id\":$ID},\"cantidad\":1}]}"
    })
    void rechazaDatosInvalidosSinCambiarInventario(String body) throws Exception {
        Producto producto = producto("5.00", 10);
        long ventasAntes = ventas.count();
        expect(400, request("POST", "/api/ventas", body.replace("$ID", producto.getId().toString())));
        assertStock(producto, 10);
        assertEquals(ventasAntes, ventas.count());
    }

    @Test
    void rechazaProductoInexistenteODesactivado() throws Exception {
        expect(404, request("POST", "/api/ventas", venta("{\"producto\":{\"id\":9223372036854775807},\"cantidad\":1}")));
        Producto producto = producto("5.00", 10);
        producto.setActivo(false);
        productos.save(producto);
        expect(409, request("POST", "/api/ventas", venta(linea(producto, 1))));
        assertStock(producto, 10);
    }

    @Test
    void dosVentasSimultaneasNoPuedenVenderElMismoStock() throws Exception {
        Producto producto = producto("5.00", 3);
        String body = venta(linea(producto, 2));
        long ventasAntes = ventas.count();
        CountDownLatch preparadas = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tareas = java.util.stream.IntStream.range(0, 2).mapToObj(i -> executor.submit(() -> {
                preparadas.countDown();
                assertTrue(inicio.await(10, TimeUnit.SECONDS));
                return request("POST", "/api/ventas", body).statusCode();
            })).toList();
            assertTrue(preparadas.await(10, TimeUnit.SECONDS));
            inicio.countDown();
            var estados = List.of(tareas.get(0).get(20, TimeUnit.SECONDS), tareas.get(1).get(20, TimeUnit.SECONDS));
            assertEquals(List.of(201, 409), estados.stream().sorted().toList());
        }
        assertStock(producto, 1);
        assertEquals(ventasAntes + 1, ventas.count());
    }

    @Test
    void eliminarVentaRestituyeStockUnaSolaVez() throws Exception {
        Producto producto = producto("5.00", 5);
        long detallesAntes = detalles.count();
        var venta = expect(201, request("POST", "/api/ventas", venta(linea(producto, 2), linea(producto, 1))));
        String ruta = "/api/ventas/" + venta.get("id").asLong();
        assertStock(producto, 2);
        assertEquals(204, request("DELETE", ruta, null).statusCode());
        assertStock(producto, 5);
        assertEquals(detallesAntes, detalles.count());
        expect(404, request("DELETE", ruta, null));
        assertStock(producto, 5);
    }

    @Test
    void detalleIndependienteCalculaImportesSinRegistrarUnaVenta() throws Exception {
        Producto producto = producto("0.10", 5);
        long ventasAntes = ventas.count();
        var detalle = expect(200, request("POST", "/api/detalle-ventas", linea(producto, 3)));
        money("0.30", detalle.get("subtotal"));
        assertEquals(producto.getNombre(), detalle.get("producto").get("nombre").asText());
        assertStock(producto, 5);
        assertEquals(ventasAntes, ventas.count());
    }

    private Producto producto(String precio, int stock) {
        return productos.save(new Producto("Prueba " + UUID.randomUUID(), "Producto de prueba",
                new BigDecimal(precio), stock, "Pruebas"));
    }

    private String linea(Producto producto, int cantidad) {
        return "{\"producto\":{\"id\":" + producto.getId() + "},\"cantidad\":" + cantidad + "}";
    }

    private String venta(String... lineas) {
        return "{\"detalles\":[" + String.join(",", lineas) + "]}";
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode expect(int status, HttpResponse<String> response) {
        assertEquals(status, response.statusCode(), response.body());
        return json.readTree(response.body());
    }

    private void assertStock(Producto producto, int esperado) {
        assertEquals(esperado, productos.findById(producto.getId()).orElseThrow().getStock());
    }

    private void money(String esperado, JsonNode valor) {
        assertEquals(0, new BigDecimal(esperado).compareTo(new BigDecimal(valor.asText())), valor.toString());
    }
}
