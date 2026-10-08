# TODO — Módulos 100% independientes (consulta, tenant, suscripcion, plan, usuarios)

**Definición de "100% independiente":** un módulo solo puede importar `shared` (kernel) e `identity`.
Ninguna dependencia directa módulo→módulo de negocio. Coste total estimado: **~45 archivos a refactorizar, ~7 a crear, 0 a eliminar (solo imports), ~20 tests afectados.**

Regla de decisión aplicable a cada dependencia:

| Tipo de dependencia | Solución | Coste |
|---|---|---|
| Excepciones/helpers compartidos | Se queda en `shared` (ya es el kernel) | 0 |
| Value Objects de identidad (TenantUUID, UsuarioUUID) | Mover a `shared/domain/valueobject` | Bajo (mecánico) |
| Repositorios/dominio de otro módulo | Puerto + adaptador en infrastructure | Alto |
| Entidad JPA ajena (@JoinColumn) | Columna UUID plana, sin join a entidad externa | Medio |
| Clase de otro módulo en la firma de un use case | DTO propio en su lugar | Bajo |
| `shared` importando módulos (ciclo) | Sacar `GlobalExceptionHandler`/`SecurityContextAuth` de shared | Medio |

---

## 1. plan — COSTE: 0

Ya es independiente: sus 12 imports cruzados (8 archivos) son solo hacia `shared`.

- [ ] Sin cambios (o verificar con test ArchUnit, ver fase 5)

## 2. tenant — COSTE: 0

Ya es independiente: sus 9 imports cruzados (9 archivos) son solo hacia `shared`.

- [ ] Sin cambios

## 3. usuarios — COSTE: ~8 archivos refactorizar, 0 crear

30 imports cruzados. Tres problemas:

**a) Fuga de infraestructura a tenant (refactor obligatorio)**
- [ ] `usuarios/infrastructure/persistence/UsuarioJpaEntity.java` — `@JoinColumn TenantJpaEntity` → columna `tenant_id` UUID plana (3 imports)
- [ ] `usuarios/infrastructure/persistence/adapter/UsuarioJpaAdapter.java` — eliminar `TenantJpaRepository` y `getReferenceById` (usar el UUID directo)
- [ ] `usuarios/infrastructure/persistence/mapper/UsuarioMapper.java` — dejar de usar `TenantMapper`/`TenantJpaEntity`, mapear UUID directo

**b) Depende de auth en application (refactor obligatorio)**
- [ ] `usuarios/application/usecase/CreateUsuarioUseCase.java` — recibe `AuthPrincipal` → cambiar por DTO propio (ej. `record CreadorPor(String email, String rol)`) o mover `AuthPrincipal` a shared
- [ ] `usuarios/presentation/UsuarioController.java` — punto de llamada (1 línea)

**c) UsuarioRol de identity (decisión)**
- [ ] Opción A (recomendada): `identity` cuenta como kernel → 0 cambios (11 archivos)
- [ ] Opción B: mover `UsuarioRol` a `shared` → 11 archivos de usuarios + ~15 de otros módulos (mecánico)

## 4. suscripcion — COSTE: ~9 archivos refactorizar, 2 crear

35 imports cruzados: 17 a `plan` (7 archivos), 6 a `tenant` (4), 12 a `shared` (ok).

**a) VOs → shared (mecánico)**
- [ ] Mover `TenantUUID` y `UsuarioUUID` a `shared/domain/valueobject` → actualiza imports en suscripcion (4), consulta (8), usuarios (5), conexion (6), auth (1), onboarding (1) ≈ **25 archivos**

**b) Depende del dominio/repositorio de plan (el caro)**
- [ ] `suscripcion/domain/Suscripcion.java` — usa `Plan`, `PlanPrecio`, `PlanUUID` → definir puerto `PlanGateway` en suscripcion (devuelve DTO propio) **[CREAR]**
- [ ] `suscripcion/domain/SuscripcionHistorialPrecio.java` — `PlanPrecioUUID` → mover VO a shared o usar UUID crudo
- [ ] `suscripcion/application/usecase/CreateSuscripcionUseCase.java` — `PlanRepository` → `PlanGateway`
- [ ] `suscripcion/application/usecase/GetMiSuscripcionUseCase.java` — igual
- [ ] `suscripcion/application/usecase/UpdateSuscripcionPlanUseCase.java` — igual
- [ ] `suscripcion/application/dto/MiSuscripcionResponse.java` — `Plan`/`PlanResponse` de plan → DTO propio
- [ ] `suscripcion/infrastructure/persistence/mapper/SuscripcionMapper.java` — `PlanUUID`/`PlanPrecioUUID`
- [ ] Adaptador `PlanJpaGateway` que sí importa plan **[CREAR]** (la dependencia migra a infrastructure)
- [ ] Tests: `CreateSuscripcionUseCaseTest`, `GetMiSuscripcionUseCaseTest`, `UpdateSuscripcionPlanUseCaseTest`

**Alternativa de menor coste:** tratar `plan` como kernel compartido → 0 cambios (recomendado si plan no va a evolucionar independiente).

## 5. consulta — COSTE: ~14 archivos refactorizar, 3 crear

43 imports cruzados (16 archivos): 6 a plan/suscripcion, 8 a tenant, 4 a usuarios, 8 a conexion.

**a) Límite mensual acoplado a plan+suscripcion (obligatorio)**
- [ ] `consulta/application/usecase/ConvertirConsultaUseCase.java` — `PlanRepository` + `SuscripcionRepository` + `SuscripcionEstado` → puerto `ValidadorLimiteConsultasPort` **[CREAR]**
- [ ] `consulta/application/usecase/RegisterConsultaUseCase.java` — igual (4 imports)
- [ ] Adaptador en infrastructure que importa plan/suscripcion **[CREAR]**

**b) VOs → shared (mecánico, cubierto por 4a)**
- [ ] `consulta/domain/ConsultaHistorial.java` (TenantUUID, UsuarioUUID)
- [ ] `consulta/domain/ConsultaHistorialRepository.java` (TenantUUID)
- [ ] `consulta/application/usecase/ListConsultasUseCase.java`, `ConvertirConsultaUseCase.java`, `RegisterConsultaUseCase.java`
- [ ] `consulta/infrastructure/persistence/.../ConsultaHistorialMapper.java`, `ConsultaHistorialJpaAdapter.java`
- [ ] `consulta/presentation/ConsultaController.java`

**c) Depende de conexion (8 archivos) — decisión de diseño**
- [ ] Opción A (recomendada): `conexion` es kernel técnico → 0 cambios
- [ ] Opción B: puertos con tipos propios → refactoring de `GeneradorSqlPort`, `SqlEjecutorPort`, `PythonSqlGeneratorAdapter`, `JdbcSqlEjecutor`, 2 use cases ≈ **10 archivos + 1 DTO de esquema** (alto)

**d) Tests afectados:** `ConvertirConsultaUseCaseTest`, `ListConsultasUseCaseTest`, `RegisterConsultaUseCaseTest` (si existe), `ConsultaHistorialTest`, `ConsultaControllerTest`

---

## Fase transversal (obligatoria, rompe ciclos en `shared`)

- [ ] **GlobalExceptionHandler** (`shared/presentation/rest/`) importa excepciones de 6 módulos → moverlo a un paquete de composición (ej. `com.saas.report.config`) o crear un `@RestControllerAdvice` por módulo **[REUBICAR o CREAR ×6]**
  - Afecta: 7+ tests que lo importan
- [ ] **SecurityContextAuth** (`shared/presentation/rest/`) importa `AuthPrincipal` (auth) y `TenantUUID` (tenant) → trasladar ambos a shared, o reducir su API a tipos de `shared`/JDK **[REFACTORIZAR]**
- [ ] Mover `TenantUUID` y `UsuarioUUID` a `shared/domain/valueobject` **[CREAR ×2 / MOVER ×2]** ≈ 25 imports

## Verificación

- [ ] Añadir test de arquitectura con ArchUnit (recomendado): `noDependenciasEntreModulos()` que falle si `com.saas.report.X` importa `com.saas.report.Y` con X,Y de negocio y Y ∉ {shared, identity} **[CREAR]**
- [ ] `mvn test` en verde tras cada fase

## Orden sugerido

1. Fase transversal (GlobalExceptionHandler, SecurityContextAuth, VOs a shared) — desbloquea todo lo demás
2. usuarios (a + b) — pequeño e independiente
3. consulta (a) — puerto de límite
4. suscripcion (b) — puerto de plan (o declarar plan kernel)
5. ArchUnit como red de seguridad permanente
