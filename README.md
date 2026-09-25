# Report (proyecto)

Resumen rápido
- Código base de la aplicación `report` (Spring Boot, empaquetado `war`).
- Aplicación en desarrollo — algunas piezas (módulos, endpoints y pruebas) pueden no estar completas.

Estado actual
- JDK: 17
- Construcción: Maven (incluye `mvnw`/`mvnw.cmd` wrappers).
- Perfil de configuración principal: `application-dev.yml` (usa variables de entorno para la conexión a la BD).
- Se añadió un paquete `identity` con clases de dominio compartidas (Shared Kernel mínimo).
- Alta de clientes **self-serve**: el cliente se incorpora solo vía `POST /self-serve/registro` (crea tenant + usuario ADMIN + suscripción al plan gratuito + conexión a su BD cifrada). El alta administrativa (`POST /admin/tenants`) fue removida; `/admin/tenants` mantiene solo consulta/activar/suspender/cancelar.
- Extracción y caché del esquema de la BD del cliente (`information_schema`) al registrarse, con credenciales cifradas en reposo (AES-GCM).
- Seguridad del endpoint público: rate limiting por IP (`SELF_SERVE_RATE_LIMIT_PER_MINUTE`, default 5) y verificación de email por token (`GET /self-serve/verificar-email?token=...`). `FRONTEND_BASE_URL` (default `http://localhost:5173`) se usa para construir el enlace de verificación.

Variables de entorno requeridas
- `JWT_SECRET` — secreto para firmar tokens JWT.
- `ENCRYPTION_KEY` — clave Base64 de 16/24/32 bytes para cifrar credenciales de BD (AES-GCM) y derivar la firma de tokens de email.
- Opcionales: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `FREE_PLAN_NAME` (default `Starter`), `SELF_SERVE_RATE_LIMIT_PER_MINUTE`, `EMAIL_VERIFICATION_EXPIRATION_MILLIS`, `FRONTEND_BASE_URL`, `CORS_ALLOWED_ORIGINS`.

Contenido importante
- Código fuente: `src/main/java`.
- Tests: `src/test/java/com/saas/report` (unitarios) y `src/test/java/com/saas/integration` (integración con Testcontainers).
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

Unitarios e integración viven en paquetes separados y se ejecutan con comandos distintos.

| Suite | Comando | Qué corre |
|---|---|---|
| Unitaria | `./mvnw test` | `*Test.java` (rápido, sin Docker) |
| Integración | `./mvnw verify -Pit` | `*IT.java` en `src/test/java/com/saas/integration` (Testcontainers) |
| Completa | `./mvnw verify` | ambas |

```bash
./mvnw test
./mvnw verify -Pit
```

Los tests de integración levantan contenedores `postgres:16-alpine` y `mysql:8.4` con Testcontainers y validan las queries de `information_schema` de cada motor. En Windows, si Testcontainers no encuentra Docker, hay que iniciar el servicio `com.docker.service` o exportar `DOCKER_HOST`.

Para correr la integración contra motores ya levantados en vez de contenedores efímeros:

```bash
./mvnw verify -Pit -Dit.postgres.url=jdbc:postgresql://localhost:5432/saas_test -Dit.mysql.url=jdbc:mysql://localhost:3306/saas_test
```

Si no hay Docker disponible ni se pasan esas URLs, los tests de integración se omiten (`[TEST OMITIDO]`) en lugar de romper la build.

Notas importantes
- Se movió el enum `UsuarioRol` al paquete `com.saas.report.identity.domain.user` para evitar duplicación entre módulos. El paquete `identity` actúa como un Shared Kernel pequeño. Archivo: `src/main/java/com/saas/report/identity/domain/user/UsuarioRol.java`.
- El Shared Kernel debe mantenerse pequeño y estable: documenta cualquier cambio público y versiona el módulo si se publica como artefacto Maven en el futuro.
- Si en el futuro los roles pasan a ser dinámicos (gestión CRUD), convierte `UsuarioRol` en una entidad `Role` dentro del mismo `identity` module y añade repositorio y migración de datos.
- En el perfil `dev` se siembra el plan gratuito inicial (`src/main/resources/db/seed-plan-gratuito.sql`) si aún no existe, para que el onboarding encuentre el plan free.
- La extracción del esquema vía `information_schema` concreta la Parte C del plan de onboarding (`docs/Plan_Onboarding_SelfServe.md`); el lenguaje natural y el agente consultor son piezas pendientes (backend Python).

Próximos pasos sugeridos
- Crear un módulo Maven independiente `report-identity` si quieres producir un artefacto compartido consumible por otros servicios.
- Añadir README y contrato de API (interfaces públicas) al `identity` para gobernanza de cambios.
- Escribir pruebas de integración que cubran el flujo de creación/registro de usuarios y validación de roles.

Contacto y propiedad
- Equipo/owner: (por definir) — documenta aquí el responsable del Shared Kernel.

Si quieres, creo el módulo Maven `report-identity` y publico un `pom.xml` mínimo para convertir `identity` en un módulo independiente.
