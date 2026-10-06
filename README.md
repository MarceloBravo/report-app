# Report

Backend SaaS B2B multi-tenant que permite a clientes finales conectar su propia base de datos
(PostgreSQL o MySQL) y consultarla en **lenguaje natural**. Este servicio gestiona el multi-tenancy,
las suscripciones y planes, los usuarios, la seguridad y orquesta la conversión
lenguaje natural → SQL y su ejecución sobre la base de datos del cliente.

Microservicio complementario (conversión NL → SQL con IA, Python/FastAPI):
<https://github.com/MarceloBravo/report-app.git>

---

## 1. Finalidad de la aplicación

`report` es la capa de control y orquestación del producto:

- **Onboarding self-serve**: un cliente se registra sin intervención humana en
  `POST /self-serve/registro`. En una sola transacción se crea el tenant, el usuario `ADMIN`, la
  suscripción al plan gratuito y la conexión cifrada a su base de datos; además se extrae el esquema
  (`information_schema`) de esa base.
- **Gestión del ciclo de vida del tenant**: activación, suspensión y cancelación, con control de
  acceso restringido al equipo de plataforma.
- **Planes y suscripciones**: catálogo de planes con precios por tramos, alta/cambio/suspensión/
  cancelación de suscripciones e historial de precios.
- **Usuarios y roles**: administration de usuarios por tenant, con roles de plataforma
  (`PLATFORM_ADMIN`, `PLATFORM_USER`) y de tenant (`ADMIN`, `VIEWER`).
- **Consultas en lenguaje natural**: persiste el historial de consultas, delega la generación del
  SQL al microservicio de IA y limita la ejecución según el plan contratado.
- **Seguridad**: JWT stateless con sesión en cookies HttpOnly (opción cruda por cabecera `Authorization`),
  defensa CSRF por `SameSite=Lax` + cabecera personalizada `X-Requested-With`, cifrado AES-GCM de
  credenciales en reposo, rate limiting por IP en el endpoint público y verificación de email por
  token HMAC.

Flujo típico de una consulta:

```
Frontend → POST /consultas/convertir → microservicio IA (Python) → SQL
         → POST /consultas            → valida suscripción + cuota → historial
```

## 2. Tecnologías utilizadas

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.1.1 (Spring MVC, Spring Data JPA / Hibernate) |
| Seguridad | Spring Security (JWT stateless, BCrypt, CORS), JJWT 0.12.6 |
| Persistencia | PostgreSQL 16 (BD de la plataforma), MySQL 8.4 (BD de clientes) |
| Driver principal | `org.postgresql:postgresql` |
| Migraciones / esquema | Hibernate `ddl-auto` (`update` en dev, `validate` en prod) + semilla SQL |
| Documentación API | springdoc-openapi 3.1.0 (Swagger UI solo en `dev`) |
| Validación | Jakarta Bean Validation |
| Observabilidad | Spring Boot Actuator (`/actuator/**`) |
| Utilidades | Lombok |
| Build | Maven 3.9 (wrapper `mvnw` / `mvnw.cmd`), packaging `war` |
| Tests | JUnit 5, Spring Security Test, Testcontainers 1.21.3 (PostgreSQL + MySQL) |
| Integración IA | Cliente HTTP hacia el microservicio Python (FastAPI) |
| Infra local | Docker + `docker-compose.yml` (PostgreSQL) |

## 3. Arquitectura

Monolito modular con **Hexagonal Architecture / Clean Architecture + DDD**. Cada módulo es una
vertical autonomous con las mismas cuatro capas; las dependencias apuntan hacia el dominio.

```
src/main/java/com/saas/report
├── ReportApplication.java        # @SpringBootApplication
├── ServletInitializer.java       # arranque como WAR
├── <módulo>/
│   ├── domain/                   # entidades, value objects, repositorios (puertos), excepciones
│   ├── application/              # casos de uso (servicios), DTOs, sanitizadores
│   ├── infrastructure/           # JPA (entity/adapter/mapper), seguridad, IA, JDBC, schema
│   └── presentation/             # @RestController
├── identity/                     # Shared Kernel mínimo (enum UsuarioRol)
└── shared/                       # transversal: excepciones, ErrorResponse, GlobalExceptionHandler
```

| Módulo | Responsabilidad |
|---|---|
| `auth` | Login, refresh, logout, registro de usuarios, emisión/revocación de tokens, filtro JWT |
| `identity` | Shared Kernel: enum `UsuarioRol` compartido entre módulos |
| `tenant` | Tenant, estados, conexión a la BD del cliente (cifrada), extracción de esquema |
| `plan` | Planes de suscripción y precios por tramos |
| `suscripcion` | Suscripciones tenant↔plan, estados e historial de precios |
| `usuarios` | CRUD de usuarios, verificación de email (token HMAC) |
| `onboarding` | Flujo de alta self-serve y rate limiting por IP |
| `consulta` | Historial de consultas NL→SQL, saneado de SQL, cuota mensual |
| `shared` | Excepciones de dominio y manejo global de errores |

Convenciones transversales:

- **Puertos/adaptadores**: el dominio define interfaces (`TenantRepository`, `GeneradorSqlPort`,
  `SqlEjecutorPort`, `SchemaExtractor`); `infrastructure` las implementa.
- **Estrategia por motor**: `SchemaQueryFactory` + `SchemaQueryProvider` resuelven las consultas a
  `information_schema` específicas de PostgreSQL o MySQL.
- **Aislamiento por tenant**: tablas compartidas con `tenant_id` + Row-Level Security en
  PostgreSQL (ver `docs/Arquitectura_Base_de_Datos_v2.md`); el contexto de tenant se propaga por
  variable de sesión `app.tenant_id`.
- **Errores**: `GlobalExceptionHandler` traduce excepciones de dominio a `ErrorResponse` JSON.

### Endpoints principales

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/login[?raw=true]` | público | Autenticación; setea cookies `access_token` + `refresh_token` (HttpOnly); con `raw=true` devuelve además los tokens en el cuerpo (Swagger/curl) |
| POST | `/auth/refresh` | público | Renueva el par de tokens leyendo la cookie `refresh_token`; no expone tokens en el cuerpo |
| POST | `/auth/logout` | autenticado | Revoca el token (header o cookie) y limpia las cookies de sesión |
| GET | `/auth/me` | autenticado | Perfil del principal actual |
| POST | `/auth/register` | ADMIN/PLATFORM_* | Alta de usuario (rol forzado a `VIEWER`) |
| POST | `/auth/admin/register` | ADMIN/PLATFORM_* | Alta de usuario con rol explícito |
| POST | `/self-serve/registro` | público (rate limit) | Alta self-serve: tenant + admin + plan free + conexión cifrada |
| GET | `/self-serve/verificar-email?token=` | público | Valida el token y marca el email como verificado |
| GET | `/admin/tenants?page=&size=&search=` | PLATFORM_* | Listado paginado de tenants; `search` filtra por nombre, subdominio o estado |
| GET | `/admin/tenants/{id}` | PLATFORM_* | Consulta de tenant |
| POST | `/admin/tenants/{id}/activate\|suspend\|cancel` | PLATFORM_* | Cambio de estado del tenant |
| POST/GET | `/admin/plans` | PLATFORM_* | Alta y consulta de planes |
| POST | `/admin/suscripciones` | PLATFORM_* | Crea suscripción tenant↔plan |
| POST | `/admin/suscripciones/{id}/cancelar\|suspender` | PLATFORM_* | Estados de la suscripción |
| PUT | `/admin/suscripciones/{id}/plan` | PLATFORM_* | Cambio de plan con historial de precio |
| POST/GET/PUT/DELETE | `/admin/usuarios` | PLATFORM_* | CRUD de usuarios (filtros `rol`, `activo`) |
| POST | `/consultas/convertir` | autenticado | NL → SQL delegando en el microservicio de IA |
| POST | `/consultas` | autenticado | Registra la consulta; valida suscripción y cuota mensual |
| GET | `/consultas?tenantId=&page=&size=` | autenticado | Historial paginado de consultas del tenant, de la más reciente a la más antigua |
| GET | `/actuator/**` | público | Salud y métricas |

Documentación interactiva (perfil `dev`): <http://localhost:8080/swagger-ui.html>

### Sesión con cookies

El frontend (Angular) se autentica con cookies HttpOnly gestionadas por el navegador:

- `access_token`: `HttpOnly`, `Path=/`, `Max-Age` = vigencia del access token.
- `refresh_token`: `HttpOnly`, `Path=/auth`, `Max-Age` = vigencia del refresh token.

`POST /auth/login` setea ambas cookies y responde un cuerpo sin tokens
(`tokenType`, `expiresIn`, `refreshExpiresIn`). Con `?raw=true` (solo login) se devuelven
además `accessToken`/`refreshToken` en el cuerpo para Swagger, curl y Postman.
`POST /auth/refresh` rota ambas cookies; `POST /auth/logout` las borra (`Max-Age=0`).

El filtro JWT es dual: primero lee la cabecera `Authorization: Bearer` y, si no existe,
la cookie `access_token`.

### Defensa CSRF por cabecera

Además de `SameSite=Lax` y del CORS con orígenes exactos, `CustomHeaderCsrfFilter` exige
la cabecera `X-Requested-With: XMLHttpRequest` en los métodos inseguros (POST/PUT/PATCH/DELETE)
cuando la request trae las cookies de sesión, y **siempre** en `POST /auth/login` (cierra el
login CSRF). Ante una violación responde `403` con el `ErrorResponse` estándar.

- **Frontend Angular** debe enviarla en todas las requests (interceptor global):

```ts
@Injectable()
export class CsrfHeaderInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<unknown>, next: HttpHandler) {
    return next.handle(req.clone({
      setHeaders: { 'X-Requested-With': 'XMLHttpRequest' },
    }));
  }
}
```

- **curl / Postman sin cookies** no cambian: si no hay cookies de sesión no se exige la
  cabecera. Para simular el flujo de navegador sí hay que agregarla:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Requested-With: XMLHttpRequest" \
  -d '{"email":"...","password":"..."}'
```

- **Swagger UI** la envía automáticamente: `CsrfHeaderSwaggerIndexTransformer` inyecta un
  script en el `index.html` que añade la cabecera a las requests de la UI (solo perfil `dev`).

## 4. Requisitos

- **JDK 17** (obligatorio)
- **Maven 3.9+** — no es necesario instalarlo, se usa el wrapper (`mvnw` / `mvnw.cmd`)
- **PostgreSQL 16** accesible (la plataforma usa PostgreSQL; la instalación local se puede levantar
  con `docker-compose.yml`)
- **Docker Desktop** — solo necesario para los tests de integración con Testcontainers
- **Microservicio de IA (Python)** — opcional en desarrollo, necesario para
  `POST /consultas/convertir`

Variables de entorno (se pueden definir en un archivo `.env` en la raíz, que la aplicación importa
automáticamente mediante `spring.config.import`):

| Variable | Obligatoria | Default | Descripción |
|---|---|---|---|
| `JWT_SECRET` | sí | — | Secreto para firmar los tokens JWT |
| `ENCRYPTION_KEY` | sí | — | Clave Base64 de 16/24/32 bytes: cifrado AES-GCM de credenciales y HMAC de los tokens de email |
| `DB_URL` | no (dev) / sí (prod) | `jdbc:postgresql://localhost:5432/report_db` | JDBC de la BD de la plataforma |
| `DB_USERNAME` | no / sí | `postgres` | Usuario de la BD de la plataforma |
| `DB_PASSWORD` | no / sí | `postgres` | Password de la BD de la plataforma |
| `DB_NAME` | no | `report_db` | Nombre de la BD (usado por `docker-compose.yml`) |
| `AI_SERVICE_BASE_URL` | no | `http://127.0.0.1:8000` | Endpoint del microservicio Python de IA |
| `FREE_PLAN_NAME` | no | `Starter` | Nombre del plan gratuito usado en el onboarding |
| `FRONTEND_BASE_URL` | no | `http://localhost:5173` | Base del frontend para el enlace de verificación de email |
| `SELF_SERVE_RATE_LIMIT_PER_MINUTE` | no | `5` | Requests por IP y minuto en `/self-serve/registro` |
| `EMAIL_VERIFICATION_EXPIRATION_MILLIS` | no | `86400000` (24 h) | Vigencia del token de verificación |
| `JWT_ACCESS_TOKEN_EXPIRATION` | no | `900000` (15 min) | Vigencia del access token |
| `JWT_REFRESH_TOKEN_EXPIRATION` | no | `604800000` (7 días) | Vigencia del refresh token |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:4200,http://localhost:5173,http://localhost:8080` | Orígenes permitidos (separados por coma) |
| `AUTH_COOKIES_SECURE` | no | `false` | Agrega `Secure` a las cookies de sesión (activar en HTTPS/prod) |
| `AUTH_COOKIES_SAME_SITE` | no | `Lax` | Atributo `SameSite` de las cookies de sesión (`Lax`/`Strict`/`None`) |

Generación de secretos de ejemplo:

```bash
# ENCRYPTION_KEY (32 bytes)
openssl rand -base64 32
# JWT_SECRET
openssl rand -base64 64
```

## 5. Puesta en marcha

### 5.1 Base de datos

Opción A — con Docker (usa las variables `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME` del entorno):

```bash
docker compose up -d postgres
```

Opción B — usar una instancia PostgreSQL existente y exportar `DB_URL`, `DB_USERNAME` y
`DB_PASSWORD`.

### 5.2 Configuración

Crear un `.env` en la raíz del proyecto con, al menos, `JWT_SECRET` y `ENCRYPTION_KEY` (más las
credenciales de la BD si no usas los defaults). El archivo está en `.gitignore`.

### 5.3 Compilar

```bash
./mvnw clean package
```

```powershell
.\mvnw.cmd clean package
```

### 5.4 Ejecutar

El perfil por defecto es `dev` (`spring.profiles.active: dev` en `application.yml`). En `dev`
Hibernate crea/actualiza el esquema (`ddl-auto: update`) y se ejecuta la semilla idempotente del
plan gratuito `Starter` (`src/main/resources/db/seed-plan-gratuito.sql`).

Linux/macOS:

```bash
DB_URL=jdbc:postgresql://localhost:5432/report_db \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

Windows PowerShell:

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
