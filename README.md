# Minimarket — Proyecto Final

API REST para la gestión de un minimarket. Incluye productos, clientes, ventas y detalles de ventas, con control de inventario, pruebas automatizadas y ejecución mediante Docker Compose.

**Tecnologías principales:** Java 21 · Spring Boot · Maven Wrapper · MySQL 8.4 · Docker Compose · Postman. Para ejecución sin Docker se dispone de un perfil local con H2. Existe también una configuración **opcional** para PostgreSQL en Supabase.

## Inicio rápido — entrega para evaluación

Esta es la forma recomendada de ejecutar el proyecto en **una computadora nueva**, sin instalar Java ni MySQL por separado: Docker construye la aplicación y arranca la base de datos. Se necesita **Git y Docker Desktop** (con Docker Compose) instalados y Docker Desktop abierto.

### 1. Descargar la rama `main`

En la Terminal (macOS/Linux), PowerShell o una terminal con Git en Windows:

```bash
git clone --branch main --single-branch https://github.com/Mauricioo-RS/Proyecto-Herramientas-de-desarrollo.git
cd Proyecto-Herramientas-de-desarrollo
```

### 2. Crear el archivo local `.env`

En **macOS/Linux**:

```bash
cp .env.example .env
```

En **Windows (PowerShell)**:

```powershell
Copy-Item .env.example .env
```

Abre `.env` en tu editor antes de levantar los contenedores. Revisa estas variables:

| Variable | Función | Configuración inicial |
| --- | --- | --- |
| `APP_PORT` | Puerto local de la API | `8081` |
| `DB_PORT` | Puerto local de MySQL | `13306` |
| `MYSQL_VOLUME_NAME` | Volumen persistente de datos | `minimarket_mysql_data` |
| `MYSQL_VOLUME_EXTERNAL` | Si Docker debe reutilizar un volumen externo | `false` en instalación nueva |
| `MYSQL_PASSWORD` | Contraseña del usuario MySQL `minimarket` | Define una clave **propia** para esta instalación |
| `MYSQL_ROOT_PASSWORD` | Contraseña de administración de MySQL | Define otra clave **propia** |

El archivo `.env.example` contiene valores demostrativos: **antes de utilizar la instalación fuera de un entorno de prueba, elige contraseñas locales nuevas**. No publiques `.env`, no lo añadas a Git y no compartas contraseñas con el profesor. Las claves de Supabase **no son necesarias** para ejecutar la modalidad Docker + MySQL.

> **Si ya tienes una instalación funcionando**, conserva su `.env` y su volumen de datos. No cambies las contraseñas de una base MySQL ya inicializada esperando que se actualicen automáticamente.

### 3. Construir e iniciar la aplicación

Con Docker Desktop iniciado y desde la carpeta del repositorio:

```bash
docker compose up -d --build --wait --wait-timeout 300
docker compose ps
```

Deben aparecer los servicios `app` y `db` con estado `healthy`. La primera construcción puede tardar más por las descargas y la ejecución de pruebas.

### 4. Comprobar la API

Abre en el navegador: **http://localhost:8081/api/productos**

Una instalación nueva puede mostrar `[]`: significa que la API responde, pero todavía no se han creado productos. Para verificar por terminal:

```bash
curl -i http://localhost:8081/api/productos
```

La respuesta esperada es `HTTP 200 OK` con una lista JSON (vacía inicialmente). Si cambiaste `APP_PORT` en `.env`, usa ese puerto en la URL.

### 5. Importar el Postman de la entrega

**[Colección original de Postman — Minimarket - Proyecto Final](postman/Minimarket%20-%20Proyecto%20Final.postman_collection.json)**

En Postman utiliza **Import → Files** y selecciona el JSON descargado del repositorio. La colección contiene nueve solicitudes para productos, clientes y ventas, con URLs que apuntan por defecto a `http://localhost:8081`.

Para hacer una prueba sencilla, abre **Listar productos → Send** y comprueba `200 OK`. Para crear registros utiliza **Registrar producto** y luego consulta o registra una venta con el ID generado por tu propia base. Los IDs `1` y `3` del archivo son ejemplos: no se garantiza que existan en otra computadora.

**Revisión necesaria en la colección original:** la solicitud **Eliminar producto** tiene texto añadido al final de la URL exportada. Antes de ejecutarla, deja únicamente `http://localhost:8081/api/productos/ID` (con un ID válido), sin `Copiar URL` ni saltos de línea. Los métodos `GET` no requieren contenido en **Body**. Las peticiones `POST`, `PUT` y `DELETE` pueden modificar los datos de prueba.

### 6. Detener el sistema sin borrar datos

```bash
docker compose stop
```

Para volver a iniciarlo:

```bash
docker compose up -d --wait
```

**No uses `docker compose down -v` si necesitas conservar los productos y las ventas**, porque puede borrar el volumen de MySQL.

### Dos copias del proyecto en la misma computadora (opcional)

Por defecto, `compose.yaml` utiliza el nombre de contenedor `minimarket-mysql` y el volumen `minimarket_mysql_data`. Si ya existe otra instalación, asigna en el `.env` de la **segunda** copia un `APP_PORT`, `DB_PORT`, `MYSQL_VOLUME_NAME` y `DB_CONTAINER_NAME` distintos. Por ejemplo, los nombres pueden ser `minimarket_entrega_mysql_data` y `minimarket-entrega-mysql`. Después ejecuta la segunda copia usando un nombre de proyecto independiente:

```bash
docker compose -p minimarket-entrega up -d --build --wait --wait-timeout 300
```

Esto evita conflictos de nombre y separa los datos. No es necesario para una instalación nueva del profesor.

---

## Documentación técnica y desarrollo

A continuación se conservan los detalles de los módulos, las reglas de negocio, las pruebas y el flujo Gitflow del proyecto.


## Documentos para el equipo

- [Avances del proyecto](https://github.com/Mauricioo-RS/Proyecto-Herramientas-de-desarrollo/blob/main/docs/01_Avances_Minimarket.docx): módulos integrados, Docker, MySQL, pruebas realizadas y funcionamiento del workflow.
- [Pendientes para el equipo](https://github.com/Mauricioo-RS/Proyecto-Herramientas-de-desarrollo/blob/main/docs/02_Pendientes_Equipo_Minimarket.docx): reparto sugerido de tareas, puesta en marcha en otra PC, colección Postman y entrega mediante Gitflow hacia `main`.

Ambos documentos corresponden a la entrega del 22 de octubre de 2026. La guía `docs/Guia_Minimarket_APIs_Postman_Docker_Gitflow.docx` se conserva como material anterior; para el estado actual utiliza los dos documentos nuevos y este README.

## Ejecución local sin Docker (H2)

Requisitos: JDK 21. Maven se ejecuta mediante el wrapper incluido. Configura `JAVA_HOME` con la ruta de tu JDK 21 y agrega su carpeta `bin` al `PATH`. En PowerShell, si tienes instalado Corretto en esta ubicación:

```
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/corretto-21.0.8"
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
java -version
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

El servicio usa H2 y guarda los datos locales en `data/`. No es necesario instalar MySQL para esta configuración. Las pruebas usan una base H2 en memoria, separada de los datos locales.

Con la aplicación iniciada, consulta `http://localhost:8080/api/productos`. También están disponibles `/api/ventas`, `/api/detalle-ventas` y `/api/v1/clientes`. Detén la aplicación con `Ctrl+C`.

## Módulos integrados

| **Módulo**         | **Ruta base**         | **Operaciones**                                   |
| ------------------ | --------------------- | ------------------------------------------------- |
| Productos          | `/api/productos`      | Crear, listar, consultar, actualizar y desactivar |
| Ventas             | `/api/ventas`         | Crear, listar, consultar y eliminar               |
| Detalles de ventas | `/api/detalle-ventas` | Crear y listar                                    |
| Clientes           | `/api/v1/clientes`    | Crear, listar, consultar, actualizar y eliminar   |

La integración conserva el historial de las cuatro ramas de funcionalidades. La rama antigua `revert-1-feature/detalledeventas-Jhordan` revierte el módulo de detalles y no forma parte de esta integración.

Cuando se ejecuta directamente con Maven, los módulos usan la misma base H2 local. `data/`, `.bloop/` y `.metals/` están excluidos de Git. Reinicia el servicio después de actualizar el código para disponer de las nuevas rutas.

Ejemplo para registrar un cliente con `POST /api/v1/clientes` en Postman (Body → raw → JSON):

```
{
  "nombre": "Ana",
  "apellido": "Prueba",
  "dni": "12345678",
  "email": "ana@example.com",
  "telefono": "987654321"
}
```

El DNI debe tener ocho dígitos y ser único. La respuesta esperada es `201`. La actualización conserva el DNI original del cliente.

## Validación y flujo de trabajo

### Registrar una venta

Al ejecutar localmente con H2 utiliza `POST http://localhost:8080/api/ventas`; con Docker utiliza el puerto `8081`. En Postman selecciona Body → raw → JSON:

```
{
  "detalles": [
    {"producto": {"id": 1}, "cantidad": 2}
  ]
}
```

La API toma el precio del catálogo, calcula los subtotales y el total con importes decimales, descuenta las unidades y devuelve `201` con los datos completos del producto. Si no envías fecha, usa la fecha del servidor. Los importes enviados por el cliente se reemplazan por los calculados. No envíes ID de venta ni ID de detalle para crear registros nuevos.

Con precio 5.00 y stock 30, vender dos unidades genera un total de 10.00 y deja stock 28. Repite `GET /api/productos/1` para comprobarlo.

- Cantidad no positiva, producto sin ID o venta sin detalles: `400`.
- Producto inexistente: `404`.
- Stock insuficiente o producto desactivado: `409`, con una explicación en el campo `detail` de la respuesta.
- Si falla cualquier detalle, se revierte toda la venta y su descuento de stock.
- Las ventas simultáneas bloquean los productos durante la operación para evitar vender más existencias de las disponibles.

`DELETE /api/ventas/{id}` elimina la venta y sus detalles y devuelve el stock que descontó esa venta. Un segundo DELETE devuelve `404`. Las ventas creadas con la versión anterior se conservan sin descontar stock retroactivamente; tampoco devuelven stock al eliminarlas, porque nunca lo descontaron. El esquema H2 se actualiza al reiniciar la aplicación.

`POST /api/detalle-ventas` mantiene la creación de detalles independientes: calcula sus importes y carga el producto, pero no representa una venta ni descuenta inventario. Para registrar una venta completa usa `/api/ventas`.

### Ejecutar las pruebas

`./mvnw clean verify` (en Windows, `.\mvnw.cmd clean verify`) compila, ejecuta las pruebas y genera el JAR. Las pruebas HTTP comprueban productos, ventas y detalles, además del ciclo de creación, consulta, actualización y eliminación de clientes y el rechazo de un DNI inválido. También verifican importes, descuentos y restitución de stock, ventas simultáneas, reversión de operaciones rechazadas y compatibilidad con la base H2 anterior.

Las funcionalidades se integran en `develop`; la versión comprobada se lleva a `main`. Las ramas de funcionalidades anteriores se conservan como historial. Para trabajo nuevo, crea una rama desde `develop` actualizado y abre un PR hacia `develop` al terminar.

El workflow `.github/workflows/ci.yml` ejecuta la verificación automática en GitHub Actions. Las pruebas actuales verifican la integración y el control de inventario, no todas las reglas de negocio del minimarket.

### Workflow de GitHub Actions

`CI Minimarket` se ejecuta al subir cambios a `main`, `develop`, `feature/**`, `release/**` o `hotfix/**`, y en Pull Requests hacia `main`, `develop` y `release/**`. También admite ejecución manual cuando el workflow esté disponible en la rama predeterminada de GitHub.

El job `Java 21, Docker y APIs`, en un runner Ubuntu de GitHub:

1. Configura Java 21 y ejecuta `bash ./mvnw -B -ntp clean verify`.
2. Genera claves temporales y una base MySQL propia para esa ejecución.
3. Construye la imagen e inicia Docker Compose, esperando ambos servicios.
4. Prueba los cuatro módulos, los cálculos de ventas y las validaciones de stock.
5. Reinicia los contenedores y comprueba que los registros persisten.
6. Conserva los informes, logs y JAR durante siete días y elimina sus recursos temporales.

Consulta las ejecuciones en [Actions del repositorio](https://github.com/Mauricioo-RS/Proyecto-Herramientas-de-desarrollo/actions). Para revisar un cambio, verifica tanto el nombre de la rama como el commit de la ejecución. Un resultado correcto en una rama anterior no valida cambios nuevos.

Este workflow valida el despliegue dentro del runner de GitHub; no despliega en la computadora del equipo. Si el profesor exige despliegue automático en una PC local, queda pendiente acordar y configurar un runner `self-hosted` y un workflow de despliegue para la versión aprobada de `main`. Véase la [documentación de GitHub Actions](https://docs.github.com/en/actions/get-started/understand-github-actions).

## Docker y MySQL — referencia técnica

Requisito: Docker Desktop iniciado con contenedores Linux y Docker Compose. El proyecto define dos servicios: `app` (Spring Boot con Java 21) y `db` (MySQL 8.4). La aplicación espera hasta que MySQL esté disponible.

Para instalar desde cero sigue la sección **Inicio rápido — entrega para evaluación** (incluye comandos de macOS/Linux y Windows). Copia `.env.example` a `.env` y elige las credenciales locales de MySQL antes de iniciar la base. Si ya existe un `.env` de una instalación en funcionamiento, consérvalo. Este archivo está excluido de Git y de la imagen.

Desde la carpeta del proyecto:

```
docker compose up -d --build --wait --wait-timeout 300
docker compose ps
```

La construcción ejecuta las pruebas de Maven antes de generar la imagen final. El primer arranque descarga Java, MySQL y las dependencias y puede tardar varios minutos. Ambos servicios deben aparecer como `healthy`.

### Probar en Postman

La API de Docker está en [**http://localhost:8081**](http://localhost:8081/). `APP_PORT` en `.env` controla este puerto; dentro del contenedor, Java escucha en 8080. Selecciona el método en el desplegable y pega solo la URL en el campo de dirección.

| **Método** | **URL para Docker**                        |
| ---------- | ------------------------------------------ |
| GET        | `http://localhost:8081/api/productos`      |
| GET        | `http://localhost:8081/api/ventas`         |
| GET        | `http://localhost:8081/api/detalle-ventas` |
| GET        | `http://localhost:8081/api/v1/clientes`    |

Para crear o modificar registros, usa las mismas rutas y cuerpos JSON de los ejemplos anteriores, cambiando el puerto a 8081. La ejecución directa con Maven usa H2 en `data/` y el puerto 8080. Docker usa una base MySQL distinta: los datos de H2 no se copian automáticamente. Usa los IDs que devuelvan tus nuevos POST.

### Datos y comandos útiles

MySQL conserva sus datos en el volumen indicado por `MYSQL_VOLUME_NAME` en `.env` (por defecto, `minimarket_mysql_data`). Se puede ver en Docker Desktop → Volumes. Los servicios `app` y `db` aparecen normalmente agrupados bajo `minimarket` en Containers; el nombre predeterminado del contenedor de base es `minimarket-mysql` y se puede cambiar mediante `DB_CONTAINER_NAME`.

La aplicación se conecta a `db:3306` por la red interna de Compose. Para usar un cliente de MySQL desde la computadora anfitriona (Windows, macOS o Linux), la dirección es `127.0.0.1`, el puerto `13306`, la base `minimarket`, el usuario `minimarket` y la contraseña es el valor de `MYSQL_PASSWORD` en `.env`. `DB_PORT` permite cambiar el puerto del anfitrión. El puerto `13306` evita utilizar el puerto habitual `3306` de posibles instalaciones preexistentes.

En una instalación nueva se recomienda mantener `MYSQL_VOLUME_EXTERNAL=false` y utilizar el volumen indicado en `.env.example`. Solo si necesitas recuperar una base **ya existente** puedes establecer `MYSQL_VOLUME_NAME` con el nombre real de ese volumen y `MYSQL_VOLUME_EXTERNAL=true`; dicho volumen debe existir antes del arranque. Consulta la [documentación de volúmenes de Docker Compose](https://docs.docker.com/reference/compose-file/volumes/).

Al reutilizar una base inicializada, las credenciales de `.env` deben coincidir con las de ese volumen: cambiar las variables de MySQL no vuelve a crear usuarios ni actualiza sus contraseñas. No conectes accidentalmente una copia de prueba al volumen de otra instalación.

```
docker compose logs --tail 50 app
docker compose logs --tail 50 db
docker compose stop
docker compose up -d --wait
```

`docker compose down` elimina los contenedores y conserva el volumen. `docker compose down -v` también elimina los volúmenes administrados por Compose (no los externos): no lo uses si quieres conservar esos datos. Las claves de `.env` se usan al crear la base por primera vez; cambiarlas en el archivo no modifica las cuentas de un volumen ya inicializado.

### Comprobación automática del despliegue

Con Python instalado y los contenedores iniciados:

```
python scripts/probar_docker.py
```

El script comprueba productos, clientes, ventas y detalles, el cálculo del precio, el descuento de stock, las respuestas 409/400 y la restitución al eliminar la venta de prueba. Genera datos propios, elimina su cliente y venta y desactiva su producto al terminar.

Para comprobar también que los datos sobreviven a un reinicio:

```
python scripts/probar_docker.py --guardar target/docker-prueba.json
docker compose stop
docker compose up -d --wait
python scripts/probar_docker.py --verificar target/docker-prueba.json
```

## Integración opcional con Supabase

El proyecto incluye `compose.supabase.yaml` para ejecutar la API con el perfil `supabase` y una base **PostgreSQL externa**. Esta alternativa requiere un proyecto Supabase propio y credenciales vigentes configuradas localmente en `.env` mediante `SUPABASE_DB_URL`, `SUPABASE_DB_USERNAME` y `SUPABASE_DB_PASSWORD`. **No son parte de la instalación básica ni deben publicarse en GitHub.**

Con una conexión Supabase configurada y el puerto `8082` disponible:

```bash
docker compose -f compose.supabase.yaml --env-file .env up -d --build --wait --wait-timeout 300
```

La API de este perfil escucha en `http://localhost:8082/api/productos`. El perfil de MySQL de Docker es independiente de Supabase; no comparten datos. Para la evaluación de una instalación descargada desde GitHub, la opción recomendada sigue siendo **Docker + MySQL**.

## Material de entrega

- [Colección original de Postman](postman/Minimarket%20-%20Proyecto%20Final.postman_collection.json)
- [Avances del proyecto](docs/01_Avances_Minimarket.docx)
- [Pendientes del equipo](docs/02_Pendientes_Equipo_Minimarket.docx)
- [Configuración de Docker Compose](compose.yaml)
- [Ejemplo de configuración local](.env.example)
- [Automatizaciones de GitHub Actions](.github/workflows/ci.yml)

> **Nota para la revisión académica:** la prueba de instalación limpia y la importación de Postman deben ejecutarse en la computadora donde se evalúa el proyecto. El estado de GitHub Actions corresponde siempre a una ejecución y un commit concretos: consulta [Actions](https://github.com/Mauricioo-RS/Proyecto-Herramientas-de-desarrollo/actions) para verificar el último resultado.
