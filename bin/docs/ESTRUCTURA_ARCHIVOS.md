# Estructura de Archivos — src/main/java/com/saas/report

Arquitectura Hexagonal (DDD) con Spring Boot. Cada módulo sigue las capas: domain → application → presentation → infrastructure.

```
src/main/java/com/saas/report/
│
├── ReportApplication.java                       # Punto de entrada @SpringBootApplication
├── ServletInitializer.java                      # Extiende SpringBootServletInitializer para despliegue como WAR
│
│
├── auth/                                        # ━━ Módulo de Autenticación y Gestión de Usuarios/Tokens ━━
│   │
│   ├── domain/
│   │   ├── user/
│   │   │   ├── Usuario.java                     # Aggregate root: usuario con id, tenant, nombre, email, password, rol, activo
│   │   │   ├── UsuarioRol.java                  # Enum: ADMIN, VIEWER — roles de usuario
│   │   │   └── UsuarioRepository.java           # Interfaz de repositorio: findByEmail, existsByEmail, save
│   │   ├── token/
│   │   │   ├── UsuarioToken.java                # Entity: par access+refresh token vinculado a un Usuario, con expiración y estado
│   │   │   └── UsuarioTokenRepository.java      # Interfaz de repositorio: findByAccessToken, findByRefreshToken, save
│   │   ├── valueobject/
│   │   │   ├── UsuarioUUID.java                 # Record value object: identidad UUID del usuario
│   │   │   └── UsuarioTokenUUID.java            # Record value object: identidad UUID del token
│   │   └── exception/
│   │       ├── InvalidCredentialsException.java # Excepción: credenciales inválidas (login fallido)
│   │       ├── EmailAlreadyExistsException.java # Excepción: email ya registrado
│   │       └── TokenRevokedException.java       # Excepción: token revocado
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── LoginUseCase.java                # Autentica usuario vía AuthenticationManager, busca por email, emite tokens
│   │   │   ├── RegisterUseCase.java             # Registra usuario: valida email único, tenant activo, codifica password
│   │   │   ├── RefreshTokenUseCase.java         # Valida refresh token, revoca el viejo, emite nuevo par de tokens
│   │   │   └── LogoutUseCase.java               # Revoca el token activo (activo=false)
│   │   ├── TokenService.java                    # Interfaz: contrato de generación/validación/claims de tokens
│   │   ├── AuthTokenIssuer.java                 # Orquesta emisión de tokens + persistencia de UsuarioToken
│   │   ├── AuthPrincipal.java                   # Record: principal autenticado (email, usuarioId, tenantId, rol) en SecurityContext
│   │   └── dto/
│   │       ├── TokenResponse.java               # Record: access token, refresh token, tipo, expiraciones
│   │       ├── LoginRequest.java                # Record con @Valid: entrada de login (email, password)
│   │       ├── RegisterRequest.java             # Record con @Valid: entrada de registro (email, password, nombre, tenantId)
│   │       └── RefreshTokenRequest.java         # Record con @Valid: entrada de refresh (refreshToken)
│   │
│   ├── presentation/
│   │   └── AuthController.java                  # @RestController /auth: login, register, admin/register, refresh, logout, me
│   │
│   └── infrastructure/
│       ├── config/
│       │   └── SecurityConfig.java              # @Configuration: filter chain stateless, JWT filter, reglas de roles, CORS, BCrypt
│       ├── security/
│       │   ├── JwtTokenProvider.java            # @Component implements TokenService: genera/parsing JWT con jjwt, claims (userId, tenantId, rol)
│       │   ├── JwtAuthorizationFilter.java      # OncePerRequestFilter: valida Bearer JWT, carga token, setea AuthPrincipal + authorities
│       │   ├── CustomUserDetailsService.java    # @Service implements UserDetailsService: carga usuario por email
│       │   ├── RestAuthenticationEntryPoint.java # Devuelve JSON 401 en acceso no autenticado
│       │   └── RestAccessDeniedHandler.java     # Devuelve JSON 403 en acceso prohibido
│       └── persistence/
│           ├── UsuarioJpaEntity.java            # @Entity tabla "usuarios": ManyToOne Tenant, password hasheado
│           ├── UsuarioTokenJpaEntity.java       # @Entity tabla "usuario_token": ManyToOne Usuario
│           ├── UsuarioJpaRepository.java        # JpaRepository: findByEmail, existsByEmail
│           ├── UsuarioTokenJpaRepository.java   # JpaRepository: findByAccessToken, findByRefreshToken
│           ├── adapter/
│           │   ├── UsuarioJpaAdapter.java       # @Component implements UsuarioRepository: mapeo domain ↔ JPA
│           │   └── UsuarioTokenJpaAdapter.java  # @Component implements UsuarioTokenRepository: mapeo domain ↔ JPA
│           └── mapper/
│               ├── UsuarioMapper.java           # Mapper estático Usuario ↔ UsuarioJpaEntity
│               └── UsuarioTokenMapper.java      # Mapper estático UsuarioToken ↔ UsuarioTokenJpaEntity
│
│
├── tenant/                                      # ━━ Módulo de Tenants (Multi-tenancy) ━━
│   │
│   ├── domain/
│   │   ├── Tenant.java                          # Aggregate root: empresa con id, nombre, subdominioSlug, TenantEstado, fechaRegistro
│   │   ├── TenantEstado.java                    # Enum: ACTIVE, SUSPENDED, CANCELED
│   │   ├── TenantRepository.java                # Interfaz de repositorio: findById, existsBySubdominioSlug, save
│   │   ├── valueobject/
│   │   │   ├── TenantUUID.java                  # Record value object: identidad UUID del tenant
│   │   │   └── TenantDbConnectionUUID.java      # Record value object: identidad UUID de conexión DB externa
│   │   ├── connection/
│   │   │   ├── TenantDbConnection.java          # Entity: conexión DB del tenant (host, puerto, dbName, user, password cifrada, tipo, schema)
│   │   │   ├── TipoConexion.java                # Enum: EXTERNAL_DB, INTERNAL_SCHEMA — tipo de conexión
│   │   │   ├── TenantDbConnectionRepository.java # Puerto de repositorio: save, findById, findByTenantId
│   │   │   └── CredentialCipher.java            # Puerto de cifrado/descifrado de credenciales de BD
│   │   ├── schema/
│   │   │   ├── ColumnaEsquema.java              # Record: columna (nombre, tipoDato, nullable, esPrimaryKey)
│   │   │   ├── TablaEsquema.java                # Record: tabla con sus columnas
│   │   │   ├── EsquemaTenant.java               # Record: esquema extraído del tenant (tablas + extraidoEn)
│   │   │   ├── SchemaExtractor.java             # Puerto: extrae EsquemaTenant desde una TenantDbConnection
│   │   │   └── EsquemaCache.java                # Puerto: caché del esquema por tenantId (buscar/guardar/invalidar)
│   │   └── exception/
│   │       └── TenantSlugAlreadyExistsException.java # Excepción: slug de subdominio ya existe
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── GetTenantUseCase.java            # Consulta tenant por ID
│   │   │   ├── ActivateTenantUseCase.java       # Reactiva tenant SUSPENDED → ACTIVE
│   │   │   ├── SuspendTenantUseCase.java        # Suspende tenant ACTIVE → SUSPENDED
│   │   │   └── CancelTenantUseCase.java         # Cancela tenant ACTIVE → CANCELED
│   │   └── dto/
│   │       └── TenantResponse.java              # Record + static from(Tenant): respuesta con todos los campos
│   │
│   ├── presentation/
│   │   └── TenantController.java                # @RestController /admin/tenants: GET + activate, suspend, cancel (solo admin)
│   │
│   └── infrastructure/
│       ├── persistence/
│       │   ├── TenantJpaEntity.java             # @Entity tabla "tenants"
│       │   ├── TenantJpaRepository.java         # JpaRepository: existsBySubdominioSlug
│       │   ├── TenantDbConnectionJpaEntity.java # @Entity tabla "tenant_db_connection"
│       │   ├── TenantDbConnectionJpaRepository.java # JpaRepository: findByTenantId
│       │   ├── adapter/
│       │   │   ├── TenantJpaAdapter.java        # @Component implements TenantRepository: mapeo domain ↔ JPA
│       │   │   └── TenantDbConnectionJpaAdapter.java # @Component implements TenantDbConnectionRepository: mapeo domain ↔ JPA
│       │   └── mapper/
│       │       ├── TenantMapper.java            # Mapper estático Tenant ↔ TenantJpaEntity
│       │       └── TenantDbConnectionMapper.java # Mapper estático TenantDbConnection ↔ JPA
│       ├── schema/
│       │   ├── JdbcSchemaExtractor.java         # @Component implements SchemaExtractor: JDBC + information_schema (PostgreSQL)
│       │   └── EsquemaCacheService.java         # @Component implements EsquemaCache: ConcurrentHashMap por tenantId
│       └── security/
│           └── AesCredentialCipher.java         # @Component implements CredentialCipher: AES-GCM para credenciales de BD
│
│
├── plan/                                        # ━━ Módulo de Planes de Suscripción y Pricing ━━
│   │
│   ├── domain/
│   │   ├── Plan.java                            # Aggregate root: plan con nombre, descripción, maxUsuarios, limiteConsultas, activo, tramos de precio
│   │   ├── PlanPrecio.java                      # Value/entity: tramo de precio (mesDesde/mesHasta, precio) con lógica aplicaParaMes()
│   │   ├── PlanRepository.java                  # Interfaz de repositorio: findById, findAll, existsByNombrePlan, save
│   │   ├── valueobject/
│   │   │   ├── PlanUUID.java                    # Record value object: identidad UUID del plan
│   │   │   ├── PlanPrecioUUID.java              # Record value object: identidad UUID del tramo de precio
│   │   │   ├── PlanMaxUsuarios.java             # Record value object: max usuarios (>= 0)
│   │   │   └── PlanLimiteConsultasMensuales.java # Record value object: límite mensual (>= -1), esIlimitado() si es -1
│   │   └── exception/
│   │       └── PlanNombreAlreadyExistsException.java # Excepción: nombre de plan ya existe
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── CreatePlanUseCase.java           # Crea plan: valida nombre único, agrega tramos de precio
│   │   │   ├── GetPlanUseCase.java              # Consulta plan por ID
│   │   │   └── ListPlansUseCase.java            # Lista todos los planes
│   │   └── dto/
│   │       ├── PlanRequest.java                 # Record con @Valid: entrada con lista anidada de precios
│   │       ├── PlanResponse.java                # Record + static from(Plan): respuesta con esIlimitado
│   │       ├── PlanPrecioRequest.java           # Record con @Valid: entrada por tramo de precio
│   │       └── PlanPrecioResponse.java          # Record + static from(PlanPrecio): respuesta del tramo
│   │
│   ├── presentation/
│   │   └── PlanController.java                  # @RestController /admin/plans: crear, obtener, listar (solo admin)
│   │
│   └── infrastructure/
│       └── persistence/
│           ├── PlanJpaEntity.java               # @Entity tabla "plans": OneToMany PlanPrecioJpaEntity
│           ├── PlanPrecioJpaEntity.java         # @Entity tabla "plan_precios": ManyToOne Plan
│           ├── PlanJpaRepository.java           # JpaRepository: existsByNombrePlan, findAll ordered by name
│           ├── adapter/
│           │   └── PlanJpaAdapter.java          # @Component implements PlanRepository: mapeo domain ↔ JPA
│           └── mapper/
│               └── PlanMapper.java              # Mapper estático Plan ↔ PlanJpaEntity (incluye precios)
│
│
├── suscripcion/                                 # ━━ Módulo de Suscripciones ━━
│   │
│   ├── domain/
│   │   ├── Suscripcion.java                     # Aggregate root: suscripción tenant→plan, fechaInicio, estado, próximaFacturación, historial precio
│   │   ├── SuscripcionEstado.java               # Enum: ACTIVE, CANCELED, SUSPENDED
│   │   ├── SuscripcionHistorialPrecio.java      # Entity: registro de precio aplicado (periodo, precio, planPrecioId) con estaVigente()
│   │   ├── SuscripcionRepository.java           # Interfaz de repositorio: findById, findByTenantIdAndEstado, save
│   │   ├── valueobject/
│   │   │   ├── SuscripcionUUID.java             # Record value object: identidad UUID de la suscripción
│   │   │   └── SuscripcionHistorialPrecioUUID.java # Record value object: identidad UUID del historial de precio
│   │   └── exception/
│   │       └── SuscripcionAlreadyActiveException.java # Excepción: tenant ya tiene suscripción activa/suspendida
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── CreateSuscripcionUseCase.java    # Crea suscripción: valida plan activo, previene duplicados, semilla historial precio
│   │   │   ├── UpdateSuscripcionPlanUseCase.java # Cambia plan de suscripción existente, crea nuevo registro de historial
│   │   │   ├── SuspendSuscripcionUseCase.java   # Suspende suscripción
│   │   │   └── CancelSuscripcionUseCase.java    # Cancela suscripción (vigente=false)
│   │   └── dto/
│   │       ├── SuscripcionRequest.java          # Record con @Valid: entrada (tenantId, planId, fechaInicio)
│   │       ├── SuscripcionResponse.java         # Record + historial anidado: respuesta completa
│   │       └── ActualizarPlanRequest.java       # Record: solicitud de cambio de plan (nuevo planId)
│   │
│   ├── presentation/
│   │   └── SuscripcionController.java           # @RestController /admin/suscripciones: crear, cancelar, suspender, cambiar plan (solo admin)
│   │
│   └── infrastructure/
│       └── persistence/
│           ├── SuscripcionJpaEntity.java        # @Entity tabla "suscripciones": OneToMany historial
│           ├── SuscripcionHistorialPrecioJpaEntity.java # @Entity tabla "suscripcion_historial_precios": ManyToOne Suscripcion
│           ├── SuscripcionJpaRepository.java    # JpaRepository: findByTenantIdAndEstado
│           ├── adapter/
│           │   └── SuscripcionJpaAdapter.java   # @Component implements SuscripcionRepository: mapeo domain ↔ JPA
│           └── mapper/
│               └── SuscripcionMapper.java       # Mapper estático Suscripcion ↔ JPA (incluye historial)
│
│
├── consulta/                                    # ━━ Módulo de Historial de Consultas SQL ━━
│   │
│   ├── domain/
│   │   ├── ConsultaHistorial.java               # Aggregate: registro de consulta (tenant, usuario, pregunta, sql, fecha, exitosa, registrosCount)
│   │   ├── ConsultaHistorialRepository.java     # Interfaz de repositorio: findById, findByTenantId, countAfterDate, save
│   │   ├── valueobject/
│   │   │   └── ConsultaHistorialUUID.java       # Record value object: identidad UUID de la consulta
│   │   └── exception/
│   │       └── LimiteConsultasExcedidoException.java # Excepción: límite mensual de consultas excedido
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── RegisterConsultaUseCase.java     # Registra consulta: valida suscripción activa, verifica límite mensual del plan, persiste
│   │   │   ├── ListConsultasUseCase.java        # Lista historial de consultas por tenant
│   │   │   └── GetConsultaUseCase.java          # Obtiene una consulta por ID
│   │   └── dto/
│   │       ├── ConsultaRequest.java             # Record con @Valid: entrada de la consulta
│   │       └── ConsultaResponse.java            # Record + static from(ConsultaHistorial): respuesta
│   │
│   ├── presentation/
│   │   └── ConsultaController.java              # @RestController /consultas: registrar, obtener, listar por tenantId
│   │
│   └── infrastructure/
│       └── persistence/
│           ├── ConsultaHistorialJpaEntity.java  # @Entity tabla "consulta_historial"
│           ├── ConsultaHistorialJpaRepository.java # JpaRepository: findByTenantId desc, countAfterDate
│           ├── adapter/
│           │   └── ConsultaHistorialJpaAdapter.java # @Component implements ConsultaHistorialRepository: mapeo domain ↔ JPA
│           └── mapper/
│               └── ConsultaHistorialMapper.java # Mapper estático ConsultaHistorial ↔ JPA
│
│
├── onboarding/                                 # ━━ Módulo de Alta Self-Serve de Clientes (proceso) ━━
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   └── OnboardClienteUseCase.java      # Orquesta en una transacción: tenant + usuario ADMIN + suscripción al plan gratuito + conexión cifrada; publica TenantRegistradoEvent y genera link de verificación de email
│   │   ├── dto/
│   │   │   ├── OnboardingRequest.java          # Record con @Valid: cliente + usuario + conexion (anidados)
│   │   │   └── OnboardingResponse.java         # Record: tenantId, usuarioId, suscripcionId, dbConnectionId, emailVerificacionUrl
│   │   └── event/
│   │       └── TenantRegistradoEvent.java      # Evento de dominio del proceso: se dispara tras persistir la conexión
│   │
│   ├── presentation/
│   │   └── OnboardingController.java           # @RestController /self-serve/registro (público, con rate limit)
│   │
│   └── infrastructure/
│       ├── TenantSchemaSyncListener.java      # @TransactionalEventListener(AFTER_COMMIT): extrae y cachea el esquema de la BD del tenant
│       └── security/
│           ├── SelfServeRateLimiter.java      # Token bucket por IP para el endpoint público
│           └── SelfServeRateLimitFilter.java  # OncePerRequestFilter: 429 cuando se excede el límite en /self-serve/registro
│
│
├── usuarios/                                   # ━━ Módulo de Gestión de Usuarios (CRUD / dominio) ━━
│   │
│   ├── domain/
│   │   ├── usuario/
│   │   │   ├── Usuario.java                    # Aggregate root: rol, activo, emailVerificado
│   │   │   └── UsuarioRepository.java          # Interfaz de repositorio: findByEmail, findById, save
│   │   ├── valueobject/
│   │   │   └── UsuarioUUID.java                # Record value object: identidad UUID del usuario
│   │   ├── emailverificacion/
│   │   │   └── EmailVerificationTokenService.java # Puerto: generar/verificar token de verificación de email
│   │   └── exception/
│   │       └── EmailAlreadyExistsException.java # Excepción: email ya registrado
│   │
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── CreateUsuarioUseCase.java       # Crea usuario (flujo admin)
│   │   │   ├── GetUsuarioUseCase.java          # Consulta usuario por ID
│   │   │   ├── ListUsuariosUseCase.java        # Lista usuarios
│   │   │   ├── UpdateUsuarioUseCase.java       # Actualiza datos de usuario
│   │   │   ├── DeleteUsuarioUseCase.java       # Elimina usuario
│   │   │   └── VerificarEmailUseCase.java      # Valida el token y marca el email como verificado
│   │   └── dto/
│   │       ├── UsuarioRequest.java             # Record con @Valid: entrada
│   │       ├── UsuarioResponse.java            # Record + static from(Usuario): respuesta
│   │       └── UpdateUsuarioRequest.java       # Record: solicitud de actualización
│   │
│   ├── presentation/
│   │   ├── UsuarioController.java              # @RestController /admin/usuarios
│   │   └── EmailVerificacionController.java    # @RestController /self-serve/verificar-email (público, GET por token)
│   │
│   └── infrastructure/
│       ├── persistence/
│       │   ├── UsuarioJpaEntity.java           # @Entity tabla "usuarios": incluye email_verificado
│       │   ├── UsuarioJpaRepository.java       # JpaRepository: findByEmail, existsByEmail
│       │   ├── adapter/
│       │   │   └── UsuarioJpaAdapter.java      # @Component implements UsuarioRepository: mapeo domain ↔ JPA
│       │   └── mapper/
│       │       └── UsuarioMapper.java          # Mapper estático Usuario ↔ UsuarioJpaEntity
│       └── security/
│           └── HmacEmailVerificationTokenService.java # @Component implements EmailVerificationTokenService (HMAC-SHA256, expiración, clave derivada de ENCRYPTION_KEY)
│
│
└── shared/                                      # ━━ Componentes Transversales ━━
    │
    ├── exception/
    │   ├── DomainException.java                 # Excepción base abstracta para todas las excepciones de dominio
    │   ├── ResourceNotFoundException.java       # Excepción: recurso no encontrado (404)
    │   ├── InvalidUUIDException.java            # Excepción: UUID inválido (400)
    │   ├── InvalidStatusException.java          # Excepción: transición de estado inválida (400)
    │   ├── InvalidIntegerValue.java             # Excepción: valor entero inválido (400)
    │   └── InvalidAmountException.java          # Excepción: monto inválido (400)
    │
    └── presentation/
        ├── rest/
        │   └── GlobalExceptionHandler.java      # @RestControllerAdvice: mapea excepciones → ErrorResponse con HTTP status adecuado
        └── dto/
            └── ErrorResponse.java               # Record: respuesta de error estandarizada (status, error, message, path, timestamp)
```

## Resumen Estadístico

| Categoría | Cantidad |
|-----------|----------|
| **Total archivos Java** | **~97** |
| domain (entities, value objects, repos, enums, exceptions) | ~36 |
| application (usecases, DTOs, services) | ~23 |
| presentation (controllers, exception handler, DTOs) | ~9 |
| infrastructure (JPA entities, repos, adapters, mappers, security, config) | ~29 |

| Módulo | Archivos | Aggregate Root |
|--------|----------|----------------|
| **auth** | 26 | `Usuario`, `UsuarioToken` |
| **tenant** | 26 | `Tenant`, `TenantDbConnection` |
| **plan** | 13 | `Plan` |
| **suscripcion** | 14 | `Suscripcion` |
| **consulta** | 10 | `ConsultaHistorial` |
| **onboarding** | 9 | *(proceso: orquesta tenant+usuario+suscripción+conexión+esquema)* |
| **usuarios** | 21 | `Usuario` |
| **shared** | 7 | *(componentes transversales)* |
| **root** | 2 | *(bootstrap)* |

## Flujo de Trabajo entre Agregados

```
┌─────────────┐    ┌──────────┐    ┌──────────────┐    ┌────────────────┐    ┌─────────────────┐
│   TENANT    │───▶│   AUTH   │───▶│     PLAN     │───▶│  SUSCRIPCION   │───▶│    CONSULTA     │
│  (crear)    │    │ (usuario │    │  (definir    │    │  (asociar      │    │  (registrar     │
│             │    │  login)  │    │   pricing)   │    │   tenant-plan) │    │   uso + límite) │
└─────────────┘    └──────────┘    └──────────────┘    └────────────────┘    └─────────────────┘

1. Se crea un Tenant (ACTIVE)
2. Se registra un Usuario vinculado al Tenant
3. Se define un Plan con tramos de precio (PlanPrecio)
4. Se crea una Suscripción que asocia Tenant ↔ Plan
5. Cada Consulta valida el límite mensual del Plan vigente
```
