# Proyecto-Herramientas-de-desarrollo

## Ejecución local

Requisitos: JDK 21. Maven se ejecuta mediante el wrapper incluido.
Configura `JAVA_HOME` con la ruta de tu JDK 21 y agrega su carpeta `bin` al `PATH`.
En PowerShell, si tienes instalado Corretto en esta ubicación:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/corretto-21.0.8"
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
java -version
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

El servicio usa H2 y guarda los datos locales en `data/`.
No es necesario instalar MySQL para esta configuración.
Las pruebas usan una base H2 en memoria, separada de los datos locales.

Con la aplicación iniciada, consulta `http://localhost:8080/api/productos`.
También están disponibles `/api/ventas`, `/api/detalle-ventas` y `/api/v1/clientes`.
Detén la aplicación con `Ctrl+C`.

## Módulos integrados

| Módulo | Ruta base | Operaciones |
| --- | --- | --- |
| Productos | `/api/productos` | Crear, listar, consultar, actualizar y desactivar |
| Ventas | `/api/ventas` | Crear, listar, consultar y eliminar |
| Detalles de ventas | `/api/detalle-ventas` | Crear y listar |
| Clientes | `/api/v1/clientes` | Crear, listar, consultar, actualizar y eliminar |

La integración conserva el historial de las cuatro ramas de funcionalidades.
La rama antigua `revert-1-feature/detalledeventas-Jhordan` revierte el módulo
de detalles y no forma parte de esta integración.

Todos los módulos usan la misma base H2 local. `data/`, `.bloop/` y `.metals/`
están excluidos de Git. Reinicia el servicio después de actualizar el código
para disponer de las nuevas rutas.

Ejemplo para registrar un cliente con `POST /api/v1/clientes` en Postman
(Body → raw → JSON):

```json
{
  "nombre": "Ana",
  "apellido": "Prueba",
  "dni": "12345678",
  "email": "ana@example.com",
  "telefono": "987654321"
}
```

El DNI debe tener ocho dígitos y ser único. La respuesta esperada es `201`.
La actualización conserva el DNI original del cliente.

## Validación y flujo de trabajo

### Registrar una venta

En Postman utiliza `POST http://localhost:8080/api/ventas`, Body → raw → JSON:

```json
{
  "detalles": [
    {"producto": {"id": 1}, "cantidad": 2}
  ]
}
```

La API toma el precio del catálogo, calcula los subtotales y el total con
importes decimales, descuenta las unidades y devuelve `201` con los datos
completos del producto. Si no envías fecha, usa la fecha del servidor.
Los importes enviados por el cliente se reemplazan por los calculados.
No envíes ID de venta ni ID de detalle para crear registros nuevos.

Con precio 5.00 y stock 30, vender dos unidades genera un total de 10.00
y deja stock 28. Repite `GET /api/productos/1` para comprobarlo.

- Cantidad no positiva, producto sin ID o venta sin detalles: `400`.
- Producto inexistente: `404`.
- Stock insuficiente o producto desactivado: `409`, con una explicación
  en el campo `detail` de la respuesta.
- Si falla cualquier detalle, se revierte toda la venta y su descuento de stock.
- Las ventas simultáneas bloquean los productos durante la operación para
  evitar vender más existencias de las disponibles.

`DELETE /api/ventas/{id}` elimina la venta y sus detalles y devuelve el stock
que descontó esa venta. Un segundo DELETE devuelve `404`.
Las ventas creadas con la versión anterior se conservan sin descontar stock
retroactivamente; tampoco devuelven stock al eliminarlas, porque nunca lo
descontaron. El esquema H2 se actualiza al reiniciar la aplicación.

`POST /api/detalle-ventas` mantiene la creación de detalles independientes:
calcula sus importes y carga el producto, pero no representa una venta ni
descuenta inventario. Para registrar una venta completa usa `/api/ventas`.

### Ejecutar las pruebas

`./mvnw clean verify` (en Windows, `.\mvnw.cmd clean verify`) compila,
ejecuta las pruebas y genera el JAR. Las pruebas HTTP comprueban productos,
ventas y detalles, además del ciclo de creación, consulta, actualización y
eliminación de clientes y el rechazo de un DNI inválido. También verifican
importes, descuentos y restitución de stock, ventas simultáneas, reversión
de operaciones rechazadas y compatibilidad con la base H2 anterior.

Las funcionalidades se integran en `develop`; la versión comprobada se lleva
a `main`. Las ramas de funcionalidades anteriores se conservan como historial.
Para trabajo nuevo, crea una rama desde `develop` actualizado y abre un PR
hacia `develop` al terminar.

GitHub Actions sigue pendiente. Las pruebas actuales verifican la
integración y el control de inventario, no todas las reglas de negocio del minimarket.
La guía Word en `docs/` describe el estado anterior a la integración de clientes;
las rutas y el estado actualizado se encuentran en este README.

## Ejecutar con Docker y MySQL

Requisito: Docker Desktop iniciado con contenedores Linux y Docker Compose.
El proyecto define dos servicios: `app` (Spring Boot con Java 21) y `db`
(MySQL 8.4). La aplicación espera hasta que MySQL esté disponible.

En una copia nueva del repositorio, prepara la configuración local en PowerShell:

```powershell
Copy-Item .env.example .env
```

Edita `.env` para definir `MYSQL_PASSWORD` y `MYSQL_ROOT_PASSWORD`.
Si ya existe `.env`, usa ese archivo. El archivo está excluido de Git y de
la imagen. Las claves de ejemplo son únicamente para desarrollo local.

Desde la carpeta del proyecto:

```powershell
docker compose up -d --build --wait --wait-timeout 300
docker compose ps
```

La construcción ejecuta las pruebas de Maven antes de generar la imagen final.
El primer arranque descarga Java, MySQL y las dependencias y puede tardar varios
minutos. Ambos servicios deben aparecer como `healthy`.

### Probar en Postman

La API de Docker está en **http://localhost:8081**. `APP_PORT` en `.env` controla
este puerto; dentro del contenedor, Java escucha en 8080.
Selecciona el método en el desplegable y pega solo la URL en el campo de dirección.

| Método | URL para Docker |
| --- | --- |
| GET | `http://localhost:8081/api/productos` |
| GET | `http://localhost:8081/api/ventas` |
| GET | `http://localhost:8081/api/detalle-ventas` |
| GET | `http://localhost:8081/api/v1/clientes` |

Para crear o modificar registros, usa las mismas rutas y cuerpos JSON de los
ejemplos anteriores, cambiando el puerto a 8081. La ejecución directa con Maven
usa H2 en `data/` y el puerto 8080. Docker usa una base MySQL distinta: los datos
de H2 no se copian automáticamente. Usa los IDs que devuelvan tus nuevos POST.

### Datos y comandos útiles

MySQL conserva sus datos en el volumen `minimarket_mysql_data`. Se puede ver
en Docker Desktop → Volumes. Los servicios `app` y `db` aparecen agrupados bajo
`minimarket` en Containers. MySQL se comunica con la API por la red interna de
Compose y no publica un puerto de base de datos en tu computadora.

```powershell
docker compose logs --tail 50 app
docker compose logs --tail 50 db
docker compose stop
docker compose start --wait
```

`docker compose down` elimina los contenedores y conserva el volumen.
`docker compose down -v` también elimina los datos de MySQL: no lo uses si quieres
conservarlos. Las claves de `.env` se usan al crear la base por primera vez;
cambiarlas en el archivo no modifica las cuentas de un volumen ya inicializado.

### Comprobación automática del despliegue

Con Python instalado y los contenedores iniciados:

```powershell
python scripts/probar_docker.py
```

El script comprueba productos, clientes, ventas y detalles, el cálculo del precio,
el descuento de stock, las respuestas 409/400 y la restitución al eliminar la
venta de prueba. Genera datos propios, elimina su cliente y venta y desactiva
su producto al terminar.

Para comprobar también que los datos sobreviven a un reinicio:

```powershell
python scripts/probar_docker.py --guardar target/docker-prueba.json
docker compose stop
docker compose start --wait
python scripts/probar_docker.py --verificar target/docker-prueba.json
```
