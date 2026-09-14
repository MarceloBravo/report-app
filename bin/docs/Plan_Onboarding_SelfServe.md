# Plan de Implementación — Alta de Clientes con Onboarding Self-Serve

Ámbito: eliminar el alta administrativa de tenants (`POST /admin/tenants`) y crear el módulo `onboarding` que realiza el alta completa del cliente (tenant + usuario + suscripción a plan gratuito + registro de datos de conexión).

**Documento de revisión técnica** · Fecha: 2026-09-11

---

## 1. Objetivo

- El cliente se registra solo mediante `POST /self-serve/registro` (público), entregando sus datos de empresa, usuario y **credenciales de conexión a su base de datos**.
- Se elimina el endpoint `POST /admin/tenants` y su use case `CreateTenantUseCase`.
- Los endpoints restantes del módulo tenant (`GET /{id}`, `activate`, `suspend`, `cancel`) se mantienen.

## 2. Parte A — Eliminar `POST /admin/tenants`

Referencias encontradas de `CreateTenantUseCase` / `TenantRequest`:

| Archivo | Acción |
|---|---|
| `src/main/java/com/saas/report/tenant/presentation/TenantController.java` | Quitar método `create()`, imports de `TenantRequest` y `CreateTenantUseCase`, y el campo + parámetro de constructor del use case. |
| `src/main/java/com/saas/report/tenant/application/usecase/CreateTenantUseCase.java` | Eliminar archivo. |
| `src/main/java/com/saas/report/tenant/application/dto/TenantRequest.java` | Eliminar archivo (solo lo usaba el use case y el controller). |
| `src/test/java/com/saas/report/tenant/application/usecase/CreateTenantUseCaseTest.java` | Eliminar archivo. |
| `src/test/java/com/saas/report/tenant/presentation/TenantControllerTest.java` | Quitar test de `create`, import de `TenantRequest`, mock y referencia a `CreateTenantUseCase`. |
| `docs/ESTRUCTURA_ARCHIVOS.md` | Actualizar: eliminar referencia al POST y al use case; señalar que `TenantRequest` ya no existe. |

> `TenantRepository.existsBySubdominioSlug` **se conserva** (lo usará el onboarding para validar slug único).

Restan utilidades: `GetTenantUseCase`, `ActivateTenantUseCase`, `SuspendTenantUseCase`, `CancelTenantUseCase` y sus tests — sin cambios.

## 3. Parte B — Módulo `onboarding` (bounded context de proceso)

Nueva estructura (solo capas presentation + application; no define agregados propios):

```
src/main/java/com/saas/report/onboarding/
├── presentation/
│   └── OnboardingController.java        # POST /self-serve/registro (permitAll)
└── application/
    ├── dto/
    │   ├── OnboardingRequest.java       # @Valid, anidados: usuario + cliente + conexion
    │   └── OnboardingResponse.java      # tenantId, usuarioId, suscripcionId, dbConnectionId
    └── usecase/
        └── OnboardClienteUseCase.java   # @Transactional, orquesta los 4 pasos
```

### 3.1 Contrato `OnboardingRequest`

```json
{
  "cliente":    { "nombreEmpresa": "…", "subdominioSlug": "…" },
  "usuario":    { "nombre": "…", "email": "…", "password": "…" },
  "conexion":   { "host": "…", "puerto": 5432, "dbName": "…",
                  "dbUser": "…", "dbPassword": "…", "schemaName": "public" }
}
```

- **No** acepta `rol` ni `tenantId` (evita escalada de privilegios: el usuario creado es siempre `ADMIN` del tenant, backend forzado).
- `tipoConexion` se fija internamente a `EXTERNAL_DB`.
- `password` del usuario → BCrypt; `dbPassword` → cifrado AES en reposo.

### 3.2 Flujo de `OnboardClienteUseCase` (una transacción)

1. Validar `existsBySubdominioSlug` → si existe, `TenantSlugAlreadyExistsException`.
2. Crear `Tenant` (estado ACTIVE) vía `TenantRepository` (misma lógica que tenía `CreateTenantUseCase`), ya que ese use case se elimina.
3. Crear el usuario `ADMIN` del tenant vía `UsuarioRepository` + `PasswordEncoder` (no reutilizar `RegisterUseCase`: exige creador autenticado/`AuthPrincipal`, diseñado para flujos admin).
4. Crear la suscripción al plan gratuito: el sistema localiza el plan free (`PlanRepository`, p. ej. por `nombrePlan` "Starter"/gratuito) y usa la misma construcción que `CreateSuscripcionUseCase` (o se refactoriza a un método interno reutilizable): estado ACTIVE, historial de precio inicial.
5. Guardar la `TenantDbConnection` **cifrada** (repositorio nuevo de la Parte C) y disparar la extracción del esquema (Módulo 1).
6. Retornar `OnboardingResponse` — **sin** incluir credenciales de BD ni password.

### 3.3 Dependencias

- El onboarding usa los **puertos** de los demás módulos: `TenantRepository`, `UsuarioRepository`, `PlanRepository`, `SuscripcionRepository`, `TenantDbConnectionRepository`. No acopla a sus implementaciones.

## 4. Parte C — Completar Módulo 1 (Conexión Segura) en `tenant`

Es prerequisito del paso 5 del onboarding. Estado actual: solo existe el modelo `TenantDbConnection` en `tenant/domain/connection/`.

- `TenantDbConnectionRepository` (interfaz de dominio: `save`, `findByTenantId`, `findById`).
- Infraestructura: `TenantDbConnectionJpaEntity`, `TenantDbConnectionJpaRepository`, adapter y mapper.
- Cifrado/descifrado de credenciales (componente `CredentialCipher`, p. ej. AES-GCM con clave de app; nunca devolver la password en DTOs).
- `SchemaExtractorService`: consulta `information_schema` (tablas, columnas, PK/FK) contra la BD externa del tenant.
- `SchemaCacheService`: caché del esquema por `tenantId` (Caffeine/`@Cacheable`) + invalidación al actualizar la conexión.

## 5. Parte D — Seguridad del endpoint público

- `permitAll` en `SecurityConfig` para `/self-serve/registro` (y seguir protegiendo `/admin/**` y el resto).
- Rate limiting (p. ej. bucket4j o a nivel de gateway) y validación de email (token de verificación).
- Nunca exponer credenciales de BD en respuestas; cifrar en reposo.
- Forceget de rol `ADMIN`: el cliente jamás elige su rol.

## 6. Orden de implementación

1. **Parte A** — eliminar endpoint + use case + tests + docs (independiente, deja la app compilando).
2. **Parte C** — repositorio de conexión, cifrado, extracción y caché de esquema (base del onboarding).
3. **Parte B** — módulo `onboarding` con su controller, DTOs, use case y registro en `SecurityConfig`.
4. **Semilla de datos** — insertar el plan gratuito inicial (Starter) para que el onboarding encuentre el plan free.
5. **Parte D** — seguridad (rate limit, verificación de email).
6. **Tests** — `OnboardClienteUseCaseTest`, `OnboardingControllerTest`, tests de conexión/cifrado; ajustar `TenantControllerTest`.
7. **Docs** — actualizar `ESTRUCTURA_ARCHIVOS.md`, `README.md` y `Diagrama_Secuencia_Consulta.md` (incorporar onboarding).

## 7. Riesgos y notas

- `RegisterUseCase` no es reutilizable para self-serve (requiere `AuthPrincipal`); se crea el usuario directamente en onboarding o se refactora `RegisterUseCase` con una variante sin creador.
- La semilla del plan gratuito debe existir antes de liberar el onboarding (fallaría `planRepository`).
- Validar límite de tenants por email/dominio para evitar abuso del trial.
- La migración de datos/limpieza de tenants existentes no se ve afectada (solo cambia el mecanismo de alta).