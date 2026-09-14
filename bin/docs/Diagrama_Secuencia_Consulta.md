# Diagrama de Secuencia — Consulta Natural a SQL

Ámbito: flujo completo de una consulta del usuario en lenguaje natural hasta la respuesta JSON, integrando el microservicio Python y el backend Spring Boot.

**Documento de revisión técnica** · Fecha: 2026-09-11

---

## Descripción textual del flujo

1. **Frontend** envía `POST /consultas` a **Spring Boot** con la pregunta en lenguaje natural, `tenantId` y `usuarioId` (autenticado con JWT).
2. **Spring Boot** valida suscripción activa y límite mensual del plan.
3. **Spring Boot** lee el esquema de tablas/columnas de ese tenant desde la **caché**.
4. **Spring Boot** delega al **Servicio Python** el esquema + la pregunta.
5. El **Servicio Python** compone el *prompt maestro* y llama a la **API de IA**.
6. La **API de IA** retorna únicamente la query SQL.
7. El **Servicio Python** devuelve el SQL a **Spring Boot**.
8. **Spring Boot** valida y sanitiza el SQL (solo `SELECT`, sin `;` ni `--`). Si falla → `400`.
9. **Spring Boot** ejecuta la query con conexión **read-only** contra la **BD Cliente** del tenant (usuario con `GRANT SELECT`).
10. La **BD Cliente** retorna registros planos.
11. **Spring Boot** mapea los registros a JSON dinámico y **registra el historial** en la **BD SaaS** (incrementa el contador del límite mensual).
12. **Spring Boot** responde al **Frontend** con `JSON { columnas, filas }`.

## Actores

| Actor | Responsabilidad |
|-------|-----------------|
| Frontend | UI web: chat + visualización dinámica (React/Angular) |
| Spring Boot | Backend corporativo: endpoint público, caché de esquema, sanitización, ejecución, registro |
| Caché | Esquema por tenant (tablas/columnas), previamente mapeado en el Módulo 1 |
| Servicio Python | Microservicio IA: compone el prompt maestro y orquesta la llamada |
| API de IA | LLM que transforma la pregunta en SQL |
| BD Cliente | PostgreSQL del tenant (solo lectura, GRANT SELECT) |
| BD SaaS | Base del SaaS: tenants, planes, suscripciones, historial de consultas |

## Medidas de seguridad en Spring Boot

- Conexiones de solo lectura: `connection.setReadOnly(true)` en el pool dinámico por tenant.
- Usuarios PostgreSQL con privilegios mínimos (`GRANT SELECT`), nunca `postgres`.
- Validador de tokens sintácticos antes de ejecutar: inicia con `SELECT`, sin `;`, sin `--`.

## Estado actual del código

- Realizado: registro de historial + validación de límite mensual (`RegisterConsultaUseCase`).
- Realizado (Parte C del onboarding): extracción del esquema del tenant (`JdbcSchemaExtractor` sobre `information_schema`) y caché por tenant (`EsquemaCacheService`), disparados tras el alta (`TenantSchemaSyncListener` AFTER_COMMIT).
- Pendiente: integración con Python, sanitización, ejecución de query, mapeo a JSON.

---

## Anexo — Alta self-serve del cliente y preparación del esquema

El esquema que conduce a la consulta natural se popula en el alta self-serve (`POST /self-serve/registro`). Flujo:

1. **Cliente web** envía `POST /self-serve/registro` (público, con rate limit por IP) con `cliente` (nombreEmpresa, subdominioSlug), `usuario` (nombre, email, password) y `conexion` (host, puerto, dbName, dbUser, dbPassword, schemaName).
2. **Spring Boot** valida que el slug y el email no existan y localiza el plan gratuito configurado (`app.onboarding.free-plan-name`, default `Starter`, sembrado en dev).
3. En una transacción: crea el **Tenant** (ACTIVE), el **Usuario** ADMIN con `emailVerificado=false` (BCrypt) y la **Suscripción** activa al plan free con su historial de precio.
4. Cifra las credenciales de la BD del cliente (**AES-GCM**, `ENCRYPTION_KEY`) y persiste la **TenantDbConnection** (tipo `EXTERNAL_DB`).
5. Publica `TenantRegistradoEvent`; tras el commit (`@TransactionalEventListener AFTER_COMMIT`), **Spring Boot** conecta a la BD del cliente (read-only), lee `information_schema` (tablas, columnas, PK, nullability) y guarda el **EsquemaTenant** en la caché por `tenantId`.
6. Responde `201` con `tenantId`, `usuarioId`, `suscripcionId`, `dbConnectionId` y `emailVerificacionUrl` (nunca credenciales ni password).
7. El cliente confirma el email con `GET /self-serve/verificar-email?token=...` (token HMAC-SHA256 con expiración); Spring valida firma/expiracion y marca el usuario verificado.

Medidas de la Parte D: rate limit token-bucket por IP (`SELF_SERVE_RATE_LIMIT_PER_MINUTE`, default 5, 429 al exceder) y verificación de email. La caché se invalida si la conexión cambia (`EsquemaCache.invalidar`).