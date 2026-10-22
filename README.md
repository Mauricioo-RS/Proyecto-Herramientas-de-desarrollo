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

`./mvnw clean verify` (en Windows, `.\mvnw.cmd clean verify`) compila,
ejecuta las pruebas y genera el JAR. Las pruebas HTTP comprueban productos,
ventas y detalles, además del ciclo de creación, consulta, actualización y
eliminación de clientes y el rechazo de un DNI inválido.

Las funcionalidades se integran en `develop`; la versión comprobada se lleva
a `main`. Las ramas de funcionalidades anteriores se conservan como historial.
Para trabajo nuevo, crea una rama desde `develop` actualizado y abre un PR
hacia `develop` al terminar.

Docker y GitHub Actions siguen pendientes. Las pruebas actuales verifican la
integración básica, no todas las reglas de negocio del minimarket.
La guía Word en `docs/` describe el estado anterior a la integración de clientes;
las rutas y el estado actualizado se encuentran en este README.
