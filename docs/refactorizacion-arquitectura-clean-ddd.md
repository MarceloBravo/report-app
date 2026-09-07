# Refactorización de arquitectura: Clean Architecture + DDD

## 1. Contexto del proyecto

Este proyecto ya muestra una base sólida de DDD y una separación inicial por capas, especialmente en el módulo `auth` y parcialmente en `tenant`.

Sin embargo, la arquitectura no está aún completamente consistente en todos los módulos. Esto se evidencia en que algunos agregados ya tienen dominio bien modelado, pero aún no cuentan con una estructura completa de `application`, `presentation` e `infrastructure`.

En particular:

- `auth`: módulo más avanzado, pero con una clase `AuthService` con demasiadas responsabilidades.
- `tenant`: mejor estructurado y sirve como referencia de arquitectura.
- `plan`, `consulta` y `suscripcion`: tienen agregados de dominio definidos, pero aún quedan incompletos en la capa de aplicación y de presentación.

Esto indica que el proyecto está en una etapa intermedia de madurez de arquitectura: buena base, pero aún requiere depuración antes de seguir agregando funcionalidades.

---

## 2. Evaluación general de arquitectura actual

### Resumen

| Aspecto | Evaluación |
|---|---|
| DDD | 74% |
| Clean Architecture | 66% |
| SOLID | 76% |
| Clean Code | 72% |
| Mantenibilidad actual | 70% |

### Observaciones

- El dominio está bien modelado en varias entidades y value objects.
- Hay separación de paquetes por capa en varios módulos.
- Existe una clara intención de usar repositorios de dominio y adaptadores de infraestructura.
- El principal problema no es la ausencia de arquitectura, sino la inconsistencia entre módulos y la mezcla de responsabilidades en servicios clave.

---

## 3. Problemas detectados

### 3.1 `AuthService` concentra demasiadas responsabilidades

La clase `AuthService` actualmente está mezclando:

- autenticación
- registro
- refresh token
- logout
- validación del tenant
- persistencia
- emisión de tokens

Esto rompe el principio SRP y dificulta la evolución del sistema.

### 3.2 Módulos incompletos

Los módulos `plan`, `consulta` y `suscripcion` tienen agregados de dominio, pero no tienen la misma madurez arquitectónica que `auth`.

Esto puede generar:

- acoplamiento visible entre capas
- duplicación de lógica
- dificultad para testear
- crecimiento desordenado del código

### 3.3 Dependencias no totalmente aisladas

Aunque hay una intención de separar capas, todavía hay una mezcla entre dominio, infraestructura y aplicación que debe reforzarse.

---

## 4. Objetivo de la refactorización

El objetivo no es reescribir el proyecto desde cero, sino:

1. dejar una estructura consistente por módulo,
2. reforzar el dominio como fuente de verdad,
3. separar casos de uso en `application`,
4. dejar `presentation` únicamente para HTTP,
5. mantener `infrastructure` como adaptadores y integraciones.

---

## 5. Arquitectura objetivo

La estructura recomendada para todos los módulos es:

```text
com.saas.report.<modulo>
├── domain
│   ├── model
│   │   ├── <Aggregate>.java
│   │   ├── <Entity>.java
│   │   └── ...
│   ├── valueobject
│   │   ├── <ValueObject>.java
│   │   └── ...
│   ├── repository
│   │   ├── <AggregateRepository>.java
│   │   └── ...
│   ├── exception
│   │   ├── <DomainException>.java
│   │   └── ...
│   └── service
│       └── <DomainService>.java
│
├── application
│   ├── usecase
│   │   ├── <CreateXUseCase>.java
│   │   ├── <GetXUseCase>.java
│   │   ├── <UpdateXUseCase>.java
│   │   └── <DeleteXUseCase>.java
│   ├── dto
│   │   ├── <Request>.java
│   │   ├── <Response>.java
│   │   └── ...
│   ├── port
│   │   ├── in
│   │   │   └── <UseCase>.java
│   │   └── out
│   │       └── <PersistencePort>.java
│   ├── mapper
│   │   └── <Mapper>.java
│   └── exception
│       └── <ApplicationException>.java
│
├── presentation
│   ├── controller
│   │   └── <Controller>.java
│   ├── dto
│   │   ├── <CreateRequest>.java
│   │   └── <Response>.java
│   └── mapper
│       └── <PresentationMapper>.java
│
└── infrastructure
    ├── persistence
    │   ├── entity
    │   │   └── <JpaEntity>.java
    │   ├── repository
    │   │   └── <JpaRepository>.java
    │   ├── adapter
    │   │   └── <RepositoryAdapter>.java
    │   └── mapper
    │       └── <DomainJpaMapper>.java
    ├── security
    │   └── ...
    ├── external
    │   └── ...
    └── config
        └── <ModuleConfig>.java
```

### Regla de dependencia

Domain → Application → Presentation

Y:

Application → Infrastructure via ports/interfaces

Nunca al revés.

---

## 6. Estructura recomendada por módulo

### 6.1 Auth

```text
auth
├── domain
│   ├── user
│   │   ├── Usuario.java
│   │   ├── UsuarioRepository.java
│   │   ├── UsuarioRol.java
│   │   └── ...
│   ├── token
│   │   ├── UsuarioToken.java
│   │   ├── UsuarioTokenRepository.java
│   │   └── ...
│   ├── valueobject
│   │   ├── UsuarioUUID.java
│   │   ├── UsuarioTokenUUID.java
│   │   └── ...
│   └── exception
│       ├── InvalidCredentialsException.java
│       ├── EmailAlreadyExistsException.java
│       └── ...
├── application
│   ├── usecase
│   │   ├── LoginUseCase.java
│   │   ├── RegisterUserUseCase.java
│   │   ├── RefreshTokenUseCase.java
│   │   └── LogoutUseCase.java
│   ├── dto
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── TokenResponse.java
│   └── service
│       ├── TokenService.java
│       └── AuthPrincipal.java
├── presentation
│   ├── controller
│   │   └── AuthController.java
│   └── dto
│       └── ...
└── infrastructure
    ├── security
    │   ├── JwtTokenProvider.java
    │   ├── JwtAuthorizationFilter.java
    │   ├── SecurityConfig.java
    │   └── ...
    ├── persistence
    │   ├── entity
    │   │   ├── UsuarioJpaEntity.java
    │   │   └── UsuarioTokenJpaEntity.java
    │   ├── mapper
    │   │   ├── UsuarioMapper.java
    │   │   └── UsuarioTokenMapper.java
    │   ├── repository
    │   │   ├── UsuarioJpaRepository.java
    │   │   └── UsuarioTokenJpaRepository.java
    │   └── adapter
    │       ├── UsuarioJpaAdapter.java
    │       └── UsuarioTokenJpaAdapter.java
    └── config
        └── AuthInfrastructureConfig.java
```

### 6.2 Tenant

```text
tenant
├── domain
│   ├── Tenant.java
│   ├── TenantRepository.java
│   ├── TenantEstado.java
│   ├── connection
│   │   ├── TenantDbConnection.java
│   │   ├── TipoConexion.java
│   │   └── ...
│   ├── valueobject
│   │   ├── TenantUUID.java
│   │   └── ...
│   └── exception
│       └── TenantException.java
├── application
│   ├── usecase
│   │   ├── CreateTenantUseCase.java
│   │   ├── ActivateTenantUseCase.java
│   │   └── SuspendTenantUseCase.java
│   ├── dto
│   │   ├── TenantRequest.java
│   │   └── TenantResponse.java
│   └── port
│       └── out
│           └── TenantPersistencePort.java
├── presentation
│   ├── controller
│   │   └── TenantController.java
│   └── dto
│       └── ...
└── infrastructure
    ├── persistence
    │   ├── entity
    │   │   └── TenantJpaEntity.java
    │   ├── mapper
    │   │   └── TenantMapper.java
    │   ├── repository
    │   │   └── TenantJpaRepository.java
    │   └── adapter
    │       └── TenantJpaAdapter.java
    └── config
        └── TenantConfig.java
```

### 6.3 Plan

```text
plan
├── domain
│   ├── Plan.java
│   ├── PlanPrecio.java
│   ├── PlanRepository.java
│   ├── valueobject
│   │   ├── PlanUUID.java
│   │   ├── PlanMaxUsuarios.java
│   │   ├── PlanLimiteConsultasMensuales.java
│   │   └── PlanPrecioUUID.java
│   └── exception
│       └── PlanException.java
├── application
│   ├── usecase
│   │   ├── CreatePlanUseCase.java
│   │   ├── GetPlanUseCase.java
│   │   └── ListPlansUseCase.java
│   ├── dto
│   │   ├── PlanRequest.java
│   │   └── PlanResponse.java
│   └── port
│       └── out
│           └── PlanPersistencePort.java
├── presentation
│   ├── controller
│   │   └── PlanController.java
│   └── dto
│       └── ...
└── infrastructure
    ├── persistence
    │   ├── entity
    │   │   └── PlanJpaEntity.java
    │   ├── mapper
    │   │   └── PlanMapper.java
    │   ├── repository
    │   │   └── PlanJpaRepository.java
    │   └── adapter
    │       └── PlanJpaAdapter.java
    └── config
        └── PlanConfig.java
```

### 6.4 Consulta

```text
consulta
├── domain
│   ├── ConsultaHistorial.java
│   ├── ConsultaHistorialRepository.java
│   ├── valueobject
│   │   ├── ConsultaHistorialUUID.java
│   │   └── ...
│   └── exception
│       └── ConsultaException.java
├── application
│   ├── usecase
│   │   ├── RegistrarConsultaUseCase.java
│   │   ├── ConsultarHistorialUseCase.java
│   │   └── ...
│   ├── dto
│   │   ├── ConsultaRequest.java
│   │   └── ConsultaResponse.java
│   └── port
│       └── out
│           └── ConsultaPersistencePort.java
├── presentation
│   ├── controller
│   │   └── ConsultaController.java
│   └── dto
│       └── ...
└── infrastructure
    ├── persistence
    │   ├── entity
    │   │   └── ConsultaHistorialJpaEntity.java
    │   ├── mapper
    │   │   └── ConsultaHistorialMapper.java
    │   ├── repository
    │   │   └── ConsultaHistorialJpaRepository.java
    │   └── adapter
    │       └── ConsultaHistorialJpaAdapter.java
    └── config
        └── ConsultaConfig.java
```

### 6.5 Suscripción

```text
suscripcion
├── domain
│   ├── Suscripcion.java
│   ├── SuscripcionEstado.java
│   ├── SuscripcionHistorialPrecio.java
│   ├── SuscripcionRepository.java
│   ├── valueobject
│   │   ├── SuscripcionUUID.java
│   │   ├── SuscripcionHistorialPrecioUUID.java
│   │   └── ...
│   └── exception
│       └── SuscripcionException.java
├── application
│   ├── usecase
│   │   ├── CrearSuscripcionUseCase.java
│   │   ├── CancelarSuscripcionUseCase.java
│   │   ├── SuspenderSuscripcionUseCase.java
│   │   └── ActualizarPlanUseCase.java
│   ├── dto
│   │   ├── SuscripcionRequest.java
│   │   └── SuscripcionResponse.java
│   └── port
│       └── out
│           └── SuscripcionPersistencePort.java
├── presentation
│   ├── controller
│   │   └── SuscripcionController.java
│   └── dto
│       └── ...
└── infrastructure
    ├── persistence
    │   ├── entity
    │   │   └── SuscripcionJpaEntity.java
    │   ├── mapper
    │   │   └── SuscripcionMapper.java
    │   ├── repository
    │   │   └── SuscripcionJpaRepository.java
    │   └── adapter
    │       └── SuscripcionJpaAdapter.java
    └── config
        └── SuscripcionConfig.java
```

---

## 7. Fases recomendadas de refactorización

### Fase 1: depurar Auth

**Objetivo:** cambiar el servicio monolítico por casos de uso.

**Resultado esperado:**

- `AuthController` orquesta use cases
- autenticación y tokens están desacoplados
- lógica de negocio queda en aplicación
- infraestructura queda separada

**Tiempo estimado:** 1 a 2 sesiones

---

### Fase 2: consolidar Tenant como referencia

**Objetivo:** dejar un módulo “limpio” y consistente que sirva como patrón.

**Resultado esperado:**

- estructura uniforme
- aceptación del patrón por todos los módulos
- mayor claridad para refactorizar los demás

**Tiempo estimado:** 1 sesión

---

### Fase 3: completar Plan

**Objetivo:** terminar el módulo con capa de aplicación y presentacional.

**Resultado esperado:**

- `Plan` con estructura consistente
- repositorio de dominio funcionando con adapter JPA
- API disponible sin lógica en controller

**Tiempo estimado:** 1 a 2 sesiones

---

### Fase 4: completar Consulta

**Objetivo:** terminar el historial de consultas como agregado funcional completo.

**Resultado esperado:**

- registrar consultas
- consultar historial
- persistencia y mapeo bien definidos

**Tiempo estimado:** 1 a 2 sesiones

---

### Fase 5: completar Suscripción

**Objetivo:** dar continuidad al agregado más complejo del negocio.

**Resultado esperado:**

- creación, suspensión, cancelación y actualización de plan bien estructuradas
- reglas del dominio centralizadas
- infraestructura desacoplada del dominio

**Tiempo estimado:** 1 a 2 sesiones

---

## 8. Qué hacer si la refactorización no termina en una sola sesión

Para poder retomarla sin perder contexto, conviene dejar una hoja de trabajo con estos puntos anotados:

### Checklist de estado

- [ ] Fase actual: ______
- [ ] Módulo actual: ______
- [ ] Clases ya creadas: ______
- [ ] Clases pendientes: ______
- [ ] Riesgos detectados: ______
- [ ] Pruebas pendientes: ______
- [ ] Siguiente paso: ______

### Registro recomendado

Se recomienda dejar una pequeña nota en el documento de sesión con:

- módulo en proceso
- objetivo de la sesión
- clases creadas/modificadas
- bloqueadores encontrados
- siguiente acción

Eso permite continuar sin que el trabajo quede inconcluso o ambiguo.

---

## 9. Reglas de calidad para no romper la arquitectura

- El dominio nunca debe depender de Spring, JPA ni HTTP.
- La capa de presentación no debe contener lógica de negocio.
- La aplicación debe orquestar casos de uso.
- La infraestructura debe implementar puertos y adaptadores.
- Cada módulo debe seguir el mismo patrón.
- Si un módulo solo tiene dominio y falta application/presentation/infrastructure, aún está incompleto.

---

## 10. Conclusión

La arquitectura del proyecto ya muestra una buena base de DDD, pero necesita una depuración estructural antes de crecer más.

La estrategia más segura es:

1. refactorizar `auth`,
2. consolidar `tenant` como referencia,
3. completar `plan`, `consulta` y `suscripcion` en la misma estructura,
4. y después continuar con nuevas funcionalidades.

Esto evita que el proyecto evolucione hacia un acoplamiento difícil de mantener y permite retomar el trabajo en sesiones futuras sin perder la coherencia.

---

## 11. Siguiente paso recomendado

La siguiente tarea concreta debería ser:

- crear el esquema de paquetes para `auth` con use cases separados,
- mover la lógica de `AuthService` a esos nuevos casos de uso,
- y dejar `AuthController` coordinando dicha lógica.

Esto consolidará la arquitectura base y servirá como patrón para el resto del proyecto.
