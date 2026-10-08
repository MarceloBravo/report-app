# Report

Backend SaaS multi-tenant para gestión de clientes, suscripciones, usuarios, autenticación y consultas en lenguaje natural sobre bases de datos de clientes.

La aplicación está construida como un monolito modular con arquitectura hexagonal / limpia, separando dominio, aplicación, infraestructura y presentación. Cada módulo tiene responsabilidades específicas y comparte mecanismos transversales como seguridad, excepciones y valor de contexto del usuario autenticado.

El sistema usa PostgreSQL para la base de datos de la plataforma y delega la generación de SQL a un servicio Python especializado que puede estar ejecutándose localmente o en un servidor remoto/externo.

---

## 1. Finalidad de la aplicación

La aplicación es la capa central del producto y orquesta la operación del SaaS:

- Onboarding self-serve: permite a un cliente registrarse sin intervención manual y crear su tenant, usuario administrador, suscripción inicial y conexión a su base de datos.
- Gestión de tenants: activación, suspensión y cancelación del cliente.
- Gestión de planes y suscripciones: catálogo, historial de precios y cambios de plan.
- Gestión de usuarios y roles: usuarios del sistema, permisos y verificación por email.
- Consultas en lenguaje natural: convierte una pregunta del usuario en SQL a través de un servicio externo de IA y valida la ejecución según la suscripción del tenant.
- Seguridad: autenticación JWT, cookies HttpOnly, filtros de seguridad, control de acceso por rol y verificación de email.

El flujo más típico es:

1. El cliente inicia sesión o se registra.
2. El backend valida el tenant y la suscripción.
3. La consulta entra por `/consultas/convertir`.
4. El servicio Python genera SQL a partir del esquema del tenant.
5. La aplicación valida cuota y ejecuta la consulta sobre la base de datos del cliente.
6. Se guarda el historial en la base de datos de la plataforma.

---

## 2. Módulos principales

El proyecto está organizado por módulos funcionales bajo `src/main/java/com/saas/report`:

- `auth`: autenticación, JWT, refresh, logout y seguridad del sistema.
- `tenant`: gestión de tenants y estados de clientes.
- `plan`: catálogo de planes y precios.
- `suscripcion`: ciclo de vida de cada suscripción.
- `usuarios`: gestión de usuarios y verificación de email.
- `onboarding`: alta autoservicio y registro inicial del cliente.
- `conexion`: gestión de conexiones a bases de datos del tenant.
- `consulta`: historial y transformación de consultas naturales a SQL.
- `shared`: utilidades transversales, DTOs, autenticación contextual y excepciones comunes.
- `identity`: definición compartida de roles del sistema.

Cada módulo está organizado siguiendo una convención similar:

- `domain`: entidad, enums, value objects, repositorios y excepciones del dominio.
- `application`: casos de uso y DTOs.
- `infrastructure`: persistencia, adaptadores, conexión a servicios externos y seguridad técnica.
- `presentation`: controladores REST.

Cada módulo tiene un README propio con su finalidad, estructura y endpoints. El README raíz describe la aplicación como conjunto.

---

## 3. Stack tecnológico

- Java 17
- Spring Boot 3.x / Spring Security
- PostgreSQL como base de datos de la plataforma
- JPA/Hibernate
- JWT + cookies HttpOnly
- Springdoc/OpenAPI
- Maven wrapper
- Servicio Python para generación de SQL con IA
- Docker Compose para levantar PostgreSQL localmente

---

## 4. Puesta en marcha

### Requisitos

- JDK 17
- Maven 3.9+ o usar el wrapper incluido (`mvnw` / `mvnw.cmd`)
- PostgreSQL 16
- Docker Desktop (opcional, para levantar PostgreSQL localmente)
- Servicio Python para IA (local o externo)

### 4.1 Base de datos PostgreSQL

La aplicación usa PostgreSQL como base de datos principal de la plataforma. Hay un `docker-compose.yml` preparado para levantar la base localmente.

Ejecutar:

```bash
docker compose up -d postgres
```

Esto levanta un contenedor con:

- usuario: según `DB_USERNAME`
- password: según `DB_PASSWORD`
- base de datos: según `DB_NAME`

Ejemplo de configuración local:

```bash
DB_URL=jdbc:postgresql://localhost:5432/report_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_NAME=report_db
```

Si ya tienes una instancia PostgreSQL externa, puedes apuntar la aplicación a esa base usando las mismas variables.

### 4.2 Servicio Python de IA

La aplicación necesita un servicio que convierta lenguaje natural en SQL. La configuración se hace con:

```properties
ai-service.base-url=${AI_SERVICE_BASE_URL:http://127.0.0.1:8000}
```

Eso significa que la aplicación intentará llamar a:

- `http://127.0.0.1:8000/sql` por defecto
- o a otra URL si se configura `AI_SERVICE_BASE_URL`

El servicio puede estar localizado en:

- un proyecto local como `D:\Prpjects\python\ai-service`
- un servidor interno de la empresa
- un proveedor externo de IA

El backend no exige que esté en el mismo equipo; solo necesita la URL correcta del servicio.

> Importante: la consulta a `/consultas/convertir` depende de ese servicio. Si no está disponible, la generación de SQL no podrá completarse.

### 4.3 Variables de entorno (.env)

La aplicación importa automáticamente un archivo `.env` en la raíz mediante:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

Se recomienda crear un archivo `.env` en la raíz del proyecto con las variables mínimas necesarias. Un ejemplo típico:

```env
JWT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
ENCRYPTION_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
DB_URL=jdbc:postgresql://localhost:5432/report_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_NAME=report_db
AI_SERVICE_BASE_URL=http://127.0.0.1:8000
FREE_PLAN_NAME=Starter
FRONTEND_BASE_URL=http://localhost:5173
SELF_SERVE_RATE_LIMIT_PER_MINUTE=5
EMAIL_VERIFICATION_EXPIRATION_MILLIS=86400000
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5173,http://localhost:8080
AUTH_COOKIES_SECURE=false
AUTH_COOKIES_SAME_SITE=Lax
```

Generar secretos en caso necesario:

```bash
openssl rand -base64 32
openssl rand -base64 64
```

Variables clave:

- `JWT_SECRET`: secreto para firmar JWT
- `ENCRYPTION_KEY`: clave Base64 para cifrar credenciales y tokens
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`: conexión a PostgreSQL
- `AI_SERVICE_BASE_URL`: URL del backend Python de IA
- `FREE_PLAN_NAME`: plan gratuito para onboarding
- `FRONTEND_BASE_URL`: base del frontend para enlace de verificación de email

### 4.4 Ejecutar la aplicación

Linux / macOS:

```bash
DB_URL=jdbc:postgresql://localhost:5432/report_db \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/report_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
.
\mvnw.cmd spring-boot:run
```

También se puede compilar primero:

```bash
./mvnw clean package
```

```powershell
.\mvnw.cmd clean package
```

La aplicación se ejecuta en el perfil `dev` por defecto. En ese perfil, el sistema intenta crear o actualizar el esquema con Hibernate y cargar la semilla inicial del plan gratuito.

---

## 5. Arquitectura funcional

La aplicación usa un diseño modular con separación por dominios.

```text
src/main/java/com/saas/report/
├── auth
├── tenant
├── plan
├── suscripcion
├── usuarios
├── onboarding
├── conexion
├── consulta
├── shared
├── identity
├── ReportApplication.java
└── ServletInitializer.java
```

Cada módulo sigue esta lógica:

- `domain`: reglas de negocio y entidades
- `application`: casos de uso y DTOs
- `infrastructure`: persistencia, seguridad, IA, adaptadores
- `presentation`: endpoints REST

---

## 6. Endpoints principales

### Autenticación

- `POST /auth/login` → iniciar sesión
- `POST /auth/login?raw=true` → devuelve tokens en cuerpo para clientes no navegador
- `POST /auth/refresh` → rotar tokens
- `POST /auth/logout` → cerrar sesión
- `GET /auth/me` → obtener perfil autenticado
- `POST /auth/register` → registrar usuario público o interno
- `POST /auth/admin/register` → registrar con rol explícito

### Onboarding / self-serve

- `POST /self-serve/registro` → alta inicial del cliente, tenant, usuario admin, conexión y suscripción gratuita
- `GET /self-serve/verificar-email?token=...` → confirmar email

### Tenants

- `GET /admin/tenants`
- `GET /admin/tenants/{tenantId}`
- `POST /admin/tenants/{tenantId}/activate`
- `POST /admin/tenants/{tenantId}/suspend`
- `POST /admin/tenants/{tenantId}/cancel`

### Planes

- `POST /admin/plans`
- `GET /admin/plans/{planId}`
- `GET /admin/plans`
- `POST /admin/plans/{planId}/activar`
- `POST /admin/plans/{planId}/desactivar`

### Suscripciones

- `POST /admin/suscripciones`
- `POST /admin/suscripciones/{suscripcionId}/cancelar`
- `POST /admin/suscripciones/{suscripcionId}/suspender`
- `PUT /admin/suscripciones/{suscripcionId}/plan`
- `GET /suscripcion/mia`

### Usuarios

- `POST /usuarios`
- `GET /usuarios/{id}`
- `GET /usuarios`
- `PUT /usuarios/{id}`
- `DELETE /usuarios/{id}`

### Consultas

- `POST /consultas/convertir`
- `POST /consultas`
- `GET /consultas/{consultaId}`
- `GET /consultas?tenantId=&page=&size=`

---

## 7. Observaciones importantes

- La base de datos principal es PostgreSQL.
- El servicio Python de IA es un componente externo que responde en `/sql` y debe estar accesible desde la aplicación.
- El proyecto puede ejecutarse con el servicio AI local o con un proveedor externo, siempre que `AI_SERVICE_BASE_URL` apunte a la URL correcta.
- La aplicación importa variables desde `.env`; este archivo no se debe versionar.
- En ambiente de desarrollo, la aplicación usa `spring.profiles.active=dev` y puede crear/actualizar el esquema automáticamente.

---

## 8. Documentación por módulo

Cada módulo del proyecto incluye un README específico. Se recomienda revisarlos para entender la estructura y los contratos de cada área del sistema:

- [src/main/java/com/saas/report/auth/README.md](src/main/java/com/saas/report/auth/README.md)
- [src/main/java/com/saas/report/conexion/README.md](src/main/java/com/saas/report/conexion/README.md)
- [src/main/java/com/saas/report/consulta/README.md](src/main/java/com/saas/report/consulta/README.md)
- [src/main/java/com/saas/report/onboarding/README.md](src/main/java/com/saas/report/onboarding/README.md)
- [src/main/java/com/saas/report/plan/README.md](src/main/java/com/saas/report/plan/README.md)
- [src/main/java/com/saas/report/shared/README.md](src/main/java/com/saas/report/shared/README.md)
- [src/main/java/com/saas/report/suscripcion/README.md](src/main/java/com/saas/report/suscripcion/README.md)
- [src/main/java/com/saas/report/tenant/README.md](src/main/java/com/saas/report/tenant/README.md)
- [src/main/java/com/saas/report/usuarios/README.md](src/main/java/com/saas/report/usuarios/README.md)


```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/report_db'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='postgres'
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`
(Swagger UI en `/swagger-ui.html` con el perfil `dev`).

Para el perfil `prod` (valida el esquema con `ddl-auto: validate` y exige `DB_URL`,
`DB_USERNAME` y `DB_PASSWORD`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

> Empaquetado `war`: `mvn package` produce `target/report.war` ejecutable en cualquier Tomcat
> mediante `ServletInitializer`.

## 6. Tests

Las suites están separadas y se ejecutan con comandos distintos.

| Suite | Comando | Qué corre |
|---|---|---|
| Unitaria | `./mvnw test` | `*Test.java` (rápido, sin Docker) |
| Integración | `./mvnw verify -Pit` | `*IT.java` en `src/test/java/com/saas/integration` (Testcontainers) |
| Completa | `./mvnw verify` | ambas |

```bash
./mvnw test
./mvnw verify -Pit
```

Los tests de integración levantan contenedores `postgres:16-alpine` y `mysql:8.4` con Testcontainers
y validan las queries de `information_schema` de cada motor. En Windows, si Testcontainers no
encuentra Docker, hay que iniciar el servicio `com.docker.service` o exportar `DOCKER_HOST`.

Para correr la integración contra motores ya levantados en lugar de contenedores efímeros:

```bash
./mvnw verify -Pit \
  -Dit.postgres.url=jdbc:postgresql://localhost:5432/saas_test \
  -Dit.mysql.url=jdbc:mysql://localhost:3306/saas_test
```

Si no hay Docker disponible ni se pasan esas URLs, los tests de integración se omiten
(`[TEST OMITIDO]`) en lugar de romper la build.

### Tests de seguridad

Las reglas de `SecurityConfig` se prueban contra la cadena real
(`SecurityConfig` + `JwtAuthorizationFilter` + entry point/denied handler) con un slice
`@WebMvcTest`, sin base de datos ni Docker. La base común es
`src/test/java/com/saas/report/support/AbstractSecurityMvcTest.java`:

```java
@WebMvcTest(controllers = MiController.class)
class MiSecurityTest extends AbstractSecurityMvcTest {

    @Test
    void viewerNoPuedeVer() throws Exception {
        mockMvc.perform(autenticadoComo(UsuarioRol.VIEWER, get("/mia")))
               .andExpect(status().isForbidden());
    }
}
```

La autenticación va por el mismo camino que en producción: se da de alta un `UsuarioToken`
en el repositorio y se manda la cabecera `Authorization: Bearer`. La base también expone
`conTokenInvalido`, `conTokenExpirado` y `conTokenRevocado` para los casos 401.

## 7. Contacto y propiedad

- Equipo/owner: Marcelo Bravo C.
