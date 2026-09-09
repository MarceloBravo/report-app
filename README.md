# Report (proyecto)

Resumen rápido
- Código base de la aplicación `report` (Spring Boot, empaquetado `war`).
- Aplicación en desarrollo — algunas piezas (módulos, endpoints y pruebas) pueden no estar completas.

Estado actual
- JDK: 17
- Construcción: Maven (incluye `mvnw`/`mvnw.cmd` wrappers).
- Perfil de configuración principal: `application-dev.yml` (usa variables de entorno para la conexión a la BD).
- Se añadió un paquete `identity` con clases de dominio compartidas (Shared Kernel mínimo).

Contenido importante
- Código fuente: `src/main/java`.
- Tests: `src/test/java` (existen tests unitarios / de integración parciales).
- Configuración dev: `src/main/resources/application-dev.yml`.

Cómo compilar

Desde la raíz del proyecto:

```bash
./mvnw clean package
```

En Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

Cómo ejecutar (desarrollo)
- Usar el perfil `dev` (por defecto `application-dev.yml`): asegúrate de tener PostgreSQL disponible o ajustar variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

Ejemplo (Linux/macOS):

```bash
DB_URL=jdbc:postgresql://localhost:5432/report_db DB_USERNAME=postgres DB_PASSWORD=postgres ./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/report_db'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='postgres'
.\mvnw.cmd spring-boot:run
```

Tests

Ejecutar la suite de pruebas con:

```bash
./mvnw test
```

Notas importantes
- Se movió el enum `UsuarioRol` al paquete `com.saas.report.identity.domain.user` para evitar duplicación entre módulos. El paquete `identity` actúa como un Shared Kernel pequeño. Archivo: `src/main/java/com/saas/report/identity/domain/user/UsuarioRol.java`.
- El Shared Kernel debe mantenerse pequeño y estable: documenta cualquier cambio público y versiona el módulo si se publica como artefacto Maven en el futuro.
- Si en el futuro los roles pasan a ser dinámicos (gestión CRUD), convierte `UsuarioRol` en una entidad `Role` dentro del mismo `identity` module y añade repositorio y migración de datos.

Próximos pasos sugeridos
- Crear un módulo Maven independiente `report-identity` si quieres producir un artefacto compartido consumible por otros servicios.
- Añadir README y contrato de API (interfaces públicas) al `identity` para gobernanza de cambios.
- Escribir pruebas de integración que cubran el flujo de creación/registro de usuarios y validación de roles.

Contacto y propiedad
- Equipo/owner: (por definir) — documenta aquí el responsable del Shared Kernel.

Si quieres, creo el módulo Maven `report-identity` y publico un `pom.xml` mínimo para convertir `identity` en un módulo independiente.
