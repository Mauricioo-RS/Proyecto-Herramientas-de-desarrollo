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
También están disponibles `/api/ventas` y `/api/detalle-ventas`.
Detén la aplicación con `Ctrl+C`.
